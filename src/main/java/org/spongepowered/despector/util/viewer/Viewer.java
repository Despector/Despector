package org.spongepowered.despector.util.viewer;

import com.google.common.base.Charsets;
import org.spongepowered.despector.Despector;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.EnumMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Viewer {

    public static EnumMap<TabType, TabData> tabs = new EnumMap<>(TabType.class);
    private static JTextField file_name_field;

    public static void onLoad(ActionEvent evt) {
        String fn = file_name_field.getText();
        System.out.println("Loading file: " + fn);

        String sourceFile = String.format("fern-decompiled/%s.java", fn.replace('.', '/'));
        File source = new File(sourceFile);
        if (source.exists() && source.isFile()) {
            try {
                tabs.get(TabType.SOURCE).update(new String(Files.readAllBytes(source.toPath()), Charsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                tabs.get(TabType.SOURCE).update("Source: " + sourceFile + " could not be loaded.");
            }
        } else {
            tabs.get(TabType.SOURCE).update("Source: " + sourceFile + " not found.");
        }

        String compFile = String.format("source/%s.class", fn.replace('.', '/'));
        File comp = new File(compFile);
        if (comp.exists() && comp.isFile()) {
            try {
                TypeEntry ast = Despector.decompile(new FileInputStream(comp));
                String str = Despector.emitToString(ast);
                tabs.get(TabType.DECOMPILED).update(str);

                StringWriter ir_writer = new StringWriter();
                JavaEmitterContext ctx = new JavaEmitterContext(ir_writer, EmitterFormat.defaults());
                Emitters.IR.emit(ctx, ast);
                tabs.get(TabType.BYTECODE).update(ir_writer.toString());
            } catch (IOException e) {
                e.printStackTrace();
                tabs.get(TabType.DECOMPILED).update("Source: " + compFile + " could not be loaded.");
                tabs.get(TabType.BYTECODE).update("Source: " + compFile + " could not be loaded.");
            }
        } else {
            tabs.get(TabType.DECOMPILED).update("Source: " + compFile + " not found.");
            tabs.get(TabType.BYTECODE).update("Source: " + compFile + " not found.");
        }

    }

    public static void main(String[] args) {

        LibraryConfiguration.parallel = false;
        LibraryConfiguration.quiet = false;

        tabs.put(TabType.SOURCE, new TabData(TabType.SOURCE));
        tabs.put(TabType.BYTECODE, new TabData(TabType.BYTECODE));
        tabs.put(TabType.DECOMPILED, new TabData(TabType.DECOMPILED));

        JFrame frame = new JFrame("Despector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 1600, 900);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        frame.setContentPane(contentPane);

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);

        file_name_field = new JTextField();
        panel.add(file_name_field);
        file_name_field.setColumns(100);
        file_name_field.setText("net.minecraft.");

        JButton loadBtn = new JButton("Load");
        panel.add(loadBtn);
        loadBtn.addActionListener(Viewer::onLoad);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(800);
        contentPane.add(splitPane, BorderLayout.CENTER);

        JTabbedPane leftPane = new JTabbedPane(JTabbedPane.TOP);
        splitPane.setLeftComponent(leftPane);

        leftPane.addTab("Source", null, new JScrollPane(tabs.get(TabType.SOURCE).left), null);
        leftPane.addTab("Bytecode", null, new JScrollPane(tabs.get(TabType.BYTECODE).left), null);
        leftPane.addTab("Decompiled", null, new JScrollPane(tabs.get(TabType.DECOMPILED).left), null);

        JTabbedPane rightPane = new JTabbedPane(JTabbedPane.TOP);
        splitPane.setRightComponent(rightPane);

        rightPane.addTab("Source", null, new JScrollPane(tabs.get(TabType.SOURCE).right), null);
        rightPane.addTab("Bytecode", null, new JScrollPane(tabs.get(TabType.BYTECODE).right), null);
        rightPane.addTab("Decompiled", null, new JScrollPane(tabs.get(TabType.DECOMPILED).right), null);

        frame.setVisible(true);
    }

}
