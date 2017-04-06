/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.emitter.format;

import com.google.common.collect.Maps;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.format.EmitterFormat.WrappingStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A loader for loading an eclipse formatter configuration.
 */
public class EclipseFormatLoader implements FormatLoader {

    public static final EclipseFormatLoader instance = new EclipseFormatLoader();
    private static final Map<String, BiConsumer<EmitterFormat, String>> settings_handlers = Maps.newHashMap();

    private static WrappingStyle wrapFromVal(int val) {
        val /= 16;
        if (val < 0 || val >= WrappingStyle.values().length) {
            return WrappingStyle.DO_NOT_WRAP;
        }
        return WrappingStyle.values()[val];
    }

    private static BracePosition braceFromVal(String val) {
        if ("next_line".equals(val)) {
            return BracePosition.NEXT_LINE;
        } else if ("next_line_shifted".equals(val)) {
            return BracePosition.NEXT_LINE_SHIFTED;
        } else if ("next_line_on_wrap".equals(val)) {
            return BracePosition.NEXT_LINE_ON_WRAP;
        }
        return BracePosition.SAME_LINE;
    }

    static {
        // @formatter:off
        BiConsumer<EmitterFormat, String> noop = (f, v)->{};
        settings_handlers.put("org.eclipse.jdt.core.compiler.source", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.continuation_indentation", (f, v) -> f.continuation_indentation = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.join_wrapped_lines", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.lineSplit", (f, v) -> f.line_split = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indentation.size", (f, v) -> f.indentation_size = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.tabulation.char", (f, v) -> f.indent_with_spaces = v.equals("space"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.tabulation.size", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_empty_lines", (f, v) -> f.indent_empty_lines = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_at_end_of_file_if_missing", (f, v) -> f.insert_new_line_at_end_of_file_if_missing = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode", noop);
        settings_handlers.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_first_class_body_declaration", (f, v) -> f.blank_lines_before_first_class_body_declaration = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_package", (f, v) -> f.blank_lines_before_package = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_after_package", (f, v) -> f.blank_lines_after_package = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_imports", (f, v) -> f.blank_lines_before_imports = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_after_imports", (f, v) -> f.blank_lines_after_imports = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_superinterfaces", (f, v) -> f.insert_space_before_comma_in_superinterfaces = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_superinterfaces", (f, v) -> f.insert_space_after_comma_in_superinterfaces = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_superclass_in_type_declaration", (f, v) -> f.alignment_for_superclass_in_type_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.align_type_members_on_columns", (f, v) -> f.align_type_members_on_columns = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_member_type", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_field", (f, v) -> f.blank_lines_before_field = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_multiple_fields", (f, v) -> f.alignment_for_multiple_fields = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", (f, v) -> f.insert_space_after_ellipsis = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_constant_arguments", (f, v) -> f.insert_space_after_comma_in_enum_constant_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_declarations", (f, v) -> f.insert_space_before_comma_in_enum_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", (f, v) -> f.insert_space_after_comma_in_enum_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_enum_constant", (f, v) -> f.insert_space_after_opening_paren_in_enum_constant = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_enum_declaration", (f, v) -> f.insert_space_before_opening_brace_in_enum_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_enum_constant", (f, v) -> f.insert_space_before_opening_paren_in_enum_constant = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_invocation_arguments", (f, v) -> f.insert_space_before_comma_in_method_invocation_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call", (f, v) -> f.alignment_for_arguments_in_explicit_constructor_call = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_explicitconstructorcall_arguments", (f, v) -> f.insert_space_before_comma_in_explicitconstructorcall_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_explicitconstructorcall_arguments", (f, v) -> f.insert_space_after_comma_in_explicitconstructorcall_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_constructor_declaration", (f, v) -> f.insert_space_before_opening_brace_in_constructor_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_constructor_declaration", (f, v) -> f.insert_space_before_opening_paren_in_constructor_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_constructor_declaration", (f, v) -> f.insert_space_after_opening_paren_in_constructor_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_parameters", (f, v) -> f.insert_space_before_comma_in_constructor_declaration_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_parameters", (f, v) -> f.insert_space_after_comma_in_constructor_declaration_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration", (f, v) -> f.alignment_for_parameters_in_constructor_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_union_type_in_multicatch", (f, v) -> f.alignment_for_union_type_in_multicatch = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_declaration", (f, v) -> f.insert_space_between_empty_parens_in_method_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_method", (f, v) -> f.blank_lines_before_method = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_method_body", (f, v) -> f.insert_new_line_in_empty_method_body = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_method_declaration", (f, v) -> f.alignment_for_method_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_throws", (f, v) -> f.insert_space_before_comma_in_method_declaration_throws = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_throws", (f, v) -> f.insert_space_after_comma_in_method_declaration_throws = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_method_declaration", (f, v) -> f.brace_position_for_method_declaration = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_parameters", (f, v) -> f.insert_space_before_comma_in_method_declaration_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_parameters", (f, v) -> f.insert_space_after_comma_in_method_declaration_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration", (f, v) -> f.alignment_for_throws_clause_in_constructor_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_declaration", (f, v) -> f.insert_space_before_opening_paren_in_method_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_type_declaration", (f, v) -> f.alignment_for_superinterfaces_in_type_declaration = wrapFromVal(Integer.parseInt(v)));
//        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_enum_constant", (f, v) -> f.alignment_for_arguments_in_enum_constant = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_between_import_groups", (f, v) -> f.blank_lines_between_import_groups = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_constant_arguments", (f, v) -> f.insert_space_before_comma_in_enum_constant_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_constructor_declaration", (f, v) -> f.brace_position_for_constructor_declaration = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_declaration_header", (f, v) -> f.indent_body_declarations_compare_to_enum_declaration_header = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration", (f, v) -> f.alignment_for_superinterfaces_in_enum_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_throws", (f, v) -> f.insert_space_after_comma_in_constructor_declaration_throws = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration", (f, v) -> f.alignment_for_parameters_in_method_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_enum_constant", (f, v) -> f.insert_space_before_closing_paren_in_enum_constant = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_enum_constants", (f, v) -> f.alignment_for_enum_constants = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_type_declaration", (f, v) -> f.brace_position_for_type_declaration = braceFromVal(v));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_method_declaration", (f, v) -> f.insert_space_before_opening_brace_in_method_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_method_declaration", (f, v) -> f.insert_space_before_closing_paren_in_method_declaration = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_block", (f, v) -> f.insert_space_before_opening_brace_in_block = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.new_lines_at_block_boundaries", (f, v) -> f.new_lines_at_block_boundaries = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.blank_lines_before_new_chunk", (f, v) -> f.blank_lines_before_new_chunk = Integer.parseInt(v));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_unary_operator", (f, v) -> f.insert_space_after_unary_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_binary_operator", (f, v) -> f.insert_space_before_binary_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_binary_operator", (f, v) -> f.insert_space_after_binary_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_prefix_operator", (f, v) -> f.insert_space_before_prefix_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_prefix_operator", (f, v) -> f.insert_space_after_prefix_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_postfix_operator", (f, v) -> f.insert_space_after_postfix_operator = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_brackets_in_array_type_reference", (f, v) -> f.insert_space_between_brackets_in_array_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_brace_in_array_initializer", (f, v) -> f.insert_space_after_opening_brace_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_before_closing_brace_in_array_initializer", (f, v) -> f.insert_new_line_before_closing_brace_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_expressions_in_array_initializer", (f, v) -> f.alignment_for_expressions_in_array_initializer = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_array_initializer", (f, v) -> f.brace_position_for_array_initializer = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_allocation_expression", (f, v) -> f.insert_space_after_opening_bracket_in_array_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_allocation_expression", (f, v) -> f.insert_space_before_closing_bracket_in_array_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_array_initializer", (f, v) -> f.insert_space_before_comma_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_type_reference", (f, v) -> f.insert_space_before_opening_bracket_in_array_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_opening_brace_in_array_initializer", (f, v) -> f.insert_new_line_after_opening_brace_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_reference", (f, v) -> f.insert_space_before_opening_bracket_in_array_reference = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_switch", (f, v) -> f.insert_space_after_opening_paren_in_switch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_switch", (f, v) -> f.insert_space_before_closing_paren_in_switch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_case", (f, v) -> f.insert_space_before_colon_in_case = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_breaks_compare_to_cases", (f, v) -> f.indent_breaks_compare_to_cases = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_case", (f, v) -> f.insert_space_after_colon_in_case = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_switch", (f, v) -> f.brace_position_for_switch = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_switch", (f, v) -> f.indent_switchstatements_compare_to_switch = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_switch", (f, v) -> f.insert_space_before_opening_brace_in_switch = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_for", (f, v) -> f.insert_space_before_opening_paren_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_for", (f, v) -> f.insert_space_after_opening_paren_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_increments", (f, v) -> f.insert_space_after_comma_in_for_increments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_for", (f, v) -> f.insert_space_before_colon_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_while", (f, v) -> f.insert_space_before_opening_paren_in_while = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_while", (f, v) -> f.insert_space_after_opening_paren_in_while = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_before_else_in_if_statement", (f, v) -> f.insert_new_line_before_else_in_if_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.keep_else_statement_on_same_line", (f, v) -> f.keep_else_statement_on_same_line = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_if", (f, v) -> f.insert_space_before_opening_paren_in_if = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_if", (f, v) -> f.insert_space_before_closing_paren_in_if = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.compact_else_if", (f, v) -> f.compact_else_if = v.equals("true"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_before_catch_in_try_statement", (f, v) -> f.insert_new_line_before_catch_in_try_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_before_finally_in_try_statement", (f, v) -> f.insert_new_line_before_finally_in_try_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_try", (f, v) -> f.insert_space_before_closing_paren_in_try = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_try", (f, v) -> f.insert_space_after_opening_paren_in_try = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_conditional_expression", (f, v) -> f.alignment_for_conditional_expression = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_method_invocation", (f, v) -> f.alignment_for_arguments_in_method_invocation = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_selector_in_method_invocation", (f, v) -> f.alignment_for_selector_in_method_invocation = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_question_in_conditional", (f, v) -> f.insert_space_after_question_in_conditional = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_question_in_wildcard", (f, v) -> f.insert_space_after_question_in_wildcard = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_lambda_arrow", (f, v) -> f.insert_space_after_lambda_arrow = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_parenthesized_expression", (f, v) -> f.insert_space_after_opening_paren_in_parenthesized_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_assignment", (f, v) -> f.blank_before_alignment_for_assignmentjavadoc_tags = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_try_resources", (f, v) -> f.insert_space_after_semicolon_in_try_resources = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_statements_compare_to_body", (f, v) -> f.indent_statements_compare_to_body = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.wrap_outer_expressions_when_nested", (f, v) -> f.wrap_outer_expressions_when_nested = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_cast", (f, v) -> f.insert_space_before_closing_paren_in_cast = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.format_guardian_clause_on_one_line", (f, v) -> f.format_guardian_clause_on_one_line = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_labeled_statement", (f, v) -> f.insert_space_after_colon_in_labeled_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_return", (f, v) -> f.insert_space_before_parenthesized_expression_in_return = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_throw", (f, v) -> f.insert_space_before_parenthesized_expression_in_throw = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_ellipsis", (f, v) -> f.insert_space_before_ellipsis = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_block", (f, v) -> f.brace_position_for_block = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_inits", (f, v) -> f.insert_space_before_comma_in_for_inits = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.wrap_before_or_operator_multicatch", (f, v) -> f.wrap_before_or_operator_multicatch = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_reference", (f, v) -> f.insert_space_before_closing_bracket_in_array_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_allocation_expression", (f, v) -> f.insert_space_before_comma_in_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_brace_in_block", (f, v) -> f.insert_space_after_closing_brace_in_block = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_throws", (f, v) -> f.insert_space_before_comma_in_constructor_declaration_throws = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_if", (f, v) -> f.insert_space_after_opening_paren_in_if = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_assignment_operator", (f, v) -> f.insert_space_after_assignment_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_assignment_operator", (f, v) -> f.insert_space_before_assignment_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_synchronized", (f, v) -> f.insert_space_after_opening_paren_in_synchronized = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_paren_in_cast", (f, v) -> f.insert_space_after_closing_paren_in_cast = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_block_in_case", (f, v) -> f.brace_position_for_block_in_case = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_catch", (f, v) -> f.insert_space_after_opening_paren_in_catch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_invocation", (f, v) -> f.insert_space_before_opening_paren_in_method_invocation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_reference", (f, v) -> f.insert_space_after_opening_bracket_in_array_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_qualified_allocation_expression", (f, v) -> f.alignment_for_arguments_in_qualified_allocation_expression = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_and_in_type_parameter", (f, v) -> f.insert_space_after_and_in_type_parameter = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_brackets_in_array_allocation_expression", (f, v) -> f.insert_space_between_empty_brackets_in_array_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.keep_empty_array_initializer_on_one_line", (f, v) -> f.keep_empty_array_initializer_on_one_line = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.continuation_indentation_for_array_initializer", (f, v) -> f.continuation_indentation_for_array_initializer = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_allocation_expression", (f, v) -> f.alignment_for_arguments_in_allocation_expression = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_lambda_body", (f, v) -> f.brace_position_for_lambda_body = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_cast", (f, v) -> f.insert_space_after_opening_paren_in_cast = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_unary_operator", (f, v) -> f.insert_space_before_unary_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.keep_imple_if_on_one_line", (f, v) -> f.keep_imple_if_on_one_line = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_labeled_statement", (f, v) -> f.insert_space_before_colon_in_labeled_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_for", (f, v) -> f.insert_space_after_colon_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_binary_expression", (f, v) -> f.alignment_for_binary_expression = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_enum_declaration", (f, v) -> f.brace_position_for_enum_declaration = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_while", (f, v) -> f.insert_space_before_closing_paren_in_while = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_try", (f, v) -> f.insert_space_before_opening_paren_in_try = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.put_empty_statement_on_new_line", (f, v) -> f.put_empty_statement_on_new_line = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_label", (f, v) -> f.insert_new_line_after_label = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_invocation", (f, v) -> f.insert_space_between_empty_parens_in_method_invocation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_before_while_in_do_statement", (f, v) -> f.insert_new_line_before_while_in_do_statement = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon", (f, v) -> f.insert_space_before_semicolon = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.number_of_blank_lines_at_beginning_of_method_body", (f, v) -> f.number_of_blank_lines_at_beginning_of_method_body = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_conditional", (f, v) -> f.insert_space_before_colon_in_conditional = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_type_header", (f, v) -> f.indent_body_declarations_compare_to_type_header = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation_type_member_declaration", (f, v) -> f.insert_space_before_opening_paren_in_annotation_type_member_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.wrap_before_binary_operator", (f, v) -> f.wrap_before_binary_operator = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_synchronized", (f, v) -> f.insert_space_before_closing_paren_in_synchronized = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_statements_compare_to_block", (f, v) -> f.indent_statements_compare_to_block = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_question_in_conditional", (f, v) -> f.insert_space_before_question_in_conditional = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_field_declarations", (f, v) -> f.insert_space_before_comma_in_multiple_field_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_compact_if", (f, v) -> f.alignment_for_compact_if = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_inits", (f, v) -> f.insert_space_after_comma_in_for_inits = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_cases", (f, v) -> f.indent_switchstatements_compare_to_cases = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_array_initializer", (f, v) -> f.insert_space_after_comma_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_default", (f, v) -> f.insert_space_before_colon_in_default = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_and_in_type_parameter", (f, v) -> f.insert_space_before_and_in_type_parameter = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_constructor_declaration", (f, v) -> f.insert_space_between_empty_parens_in_constructor_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_assert", (f, v) -> f.insert_space_after_colon_in_assert = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_method_declaration", (f, v) -> f.alignment_for_throws_clause_in_method_declaration = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_allocation_expression", (f, v) -> f.insert_space_before_opening_bracket_in_array_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_anonymous_type_declaration", (f, v) -> f.insert_new_line_in_empty_anonymous_type_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_conditional", (f, v) -> f.insert_space_after_colon_in_conditional = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_for", (f, v) -> f.insert_space_before_closing_paren_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_postfix_operator", (f, v) -> f.insert_space_before_postfix_operator = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_synchronized", (f, v) -> f.insert_space_before_opening_paren_in_synchronized = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_allocation_expression", (f, v) -> f.insert_space_after_comma_in_allocation_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_brace_in_array_initializer", (f, v) -> f.insert_space_before_closing_brace_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_resources_in_try", (f, v) -> f.alignment_for_resources_in_try = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_lambda_arrow", (f, v) -> f.insert_space_before_lambda_arrow = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_block", (f, v) -> f.insert_new_line_in_empty_block = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_parenthesized_expression", (f, v) -> f.insert_space_before_closing_paren_in_parenthesized_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_parenthesized_expression", (f, v) -> f.insert_space_before_opening_paren_in_parenthesized_expression = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_catch", (f, v) -> f.insert_space_before_closing_paren_in_catch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_local_declarations", (f, v) -> f.insert_space_before_comma_in_multiple_local_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_switch", (f, v) -> f.insert_space_before_opening_paren_in_switch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_increments", (f, v) -> f.insert_space_before_comma_in_for_increments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_invocation", (f, v) -> f.insert_space_after_opening_paren_in_method_invocation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_assert", (f, v) -> f.insert_space_before_colon_in_assert = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_array_initializer", (f, v) -> f.insert_space_before_opening_brace_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_braces_in_array_initializer", (f, v) -> f.insert_space_between_empty_braces_in_array_initializer = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_declaration", (f, v) -> f.insert_space_after_opening_paren_in_method_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_for", (f, v) -> f.insert_space_before_semicolon_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_catch", (f, v) -> f.insert_space_before_opening_paren_in_catch = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_parameterized_type_reference", (f, v) -> f.insert_space_before_opening_angle_bracket_in_parameterized_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_field_declarations", (f, v) -> f.insert_space_after_comma_in_multiple_field_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_invocation_arguments", (f, v) -> f.insert_space_after_comma_in_method_invocation_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_local_declarations", (f, v) -> f.insert_space_after_comma_in_multiple_local_declarations = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_constant_header", (f, v) -> f.indent_body_declarations_compare_to_enum_constant_header = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_for", (f, v) -> f.insert_space_after_semicolon_in_for = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_try_resources", (f, v) -> f.insert_space_before_semicolon_in_try_resources = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.keep_then_statement_on_same_line", (f, v) -> f.keep_then_statement_on_same_line = v.equals("true"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_anonymous_type_declaration", (f, v) -> f.brace_position_for_anonymous_type_declaration = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_anonymous_type_declaration", (f, v) -> f.insert_space_before_opening_brace_in_anonymous_type_declaration = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_question_in_wildcard", (f, v) -> f.insert_space_before_question_in_wildcard = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_type_declaration", (f, v) -> f.insert_space_before_opening_brace_in_type_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_parameterized_type_reference", (f, v) -> f.insert_space_before_closing_angle_bracket_in_parameterized_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_parameterized_type_reference", (f, v) -> f.insert_space_before_comma_in_parameterized_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_parameterized_type_reference", (f, v) -> f.insert_space_after_opening_angle_bracket_in_parameterized_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_parameterized_type_reference", (f, v) -> f.insert_space_after_comma_in_parameterized_type_reference = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_arguments", (f, v) -> f.insert_space_before_opening_angle_bracket_in_type_arguments = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_parameters", (f, v) -> f.insert_space_before_comma_in_type_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_parameters", (f, v) -> f.insert_space_after_comma_in_type_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_parameters", (f, v) -> f.insert_space_before_opening_angle_bracket_in_type_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_parameters", (f, v) -> f.insert_space_after_closing_angle_bracket_in_type_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_parameters", (f, v) -> f.insert_space_after_opening_angle_bracket_in_type_parameters = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_parameters", (f, v) -> f.insert_space_before_closing_angle_bracket_in_type_parameters = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_arguments", (f, v) -> f.insert_space_after_comma_in_type_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_arguments", (f, v) -> f.insert_space_before_closing_angle_bracket_in_type_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_arguments", (f, v) -> f.insert_space_after_closing_angle_bracket_in_type_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_arguments", (f, v) -> f.insert_space_before_comma_in_type_arguments = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_arguments", (f, v) -> f.insert_space_after_opening_angle_bracket_in_type_arguments = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_annotation", (f, v) -> f.insert_space_after_comma_in_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_annotation_declaration", (f, v) -> f.insert_new_line_in_empty_annotation_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_annotation", (f, v) -> f.insert_space_after_opening_paren_in_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_annotation_type_member_declaration", (f, v) -> f.insert_space_between_empty_parens_in_annotation_type_member_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_annotation_type_declaration", (f, v) -> f.insert_space_before_opening_brace_in_annotation_type_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation", (f, v) -> f.insert_space_after_at_in_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_local_variable", (f, v) -> f.insert_new_line_after_annotation_on_local_variable = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_method", (f, v) -> f.insert_new_line_after_annotation_on_method = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.brace_position_for_annotation_type_declaration", (f, v) -> f.brace_position_for_annotation_type_declaration = braceFromVal(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_annotation", (f, v) -> f.insert_space_before_comma_in_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation_type_declaration", (f, v) -> f.insert_space_after_at_in_annotation_type_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_field", (f, v) -> f.insert_new_line_after_annotation_on_field = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation", (f, v) -> f.insert_space_before_opening_paren_in_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_at_in_annotation_type_declaration", (f, v) -> f.insert_space_before_at_in_annotation_type_declaration = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_type_annotation", (f, v) -> f.insert_new_line_after_type_annotation = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_type", (f, v) -> f.insert_new_line_after_annotation_on_type = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_parameter", (f, v) -> f.insert_new_line_after_annotation_on_parameter = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_package", (f, v) -> f.insert_new_line_after_annotation_on_package = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_annotation", (f, v) -> f.alignment_for_arguments_in_annotation = wrapFromVal(Integer.parseInt(v)));
        settings_handlers.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_annotation_declaration_header", (f, v) -> f.indent_body_declarations_compare_to_annotation_declaration_header = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_annotation", (f, v) -> f.insert_space_before_closing_paren_in_annotation = v.equals("insert"));

        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_line_comments", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_block_comment", (f, v) -> f.clear_blank_lines_in_block_comment = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.insert_new_line_before_root_tags", (f, v) -> f.insert_new_line_before_root_tags = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.insert_new_line_for_parameter", (f, v) -> f.insert_new_line_for_parameter = v.equals("insert"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.indent_parameter_description", (f, v) -> f.indent_parameter_description = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.preserve_white_space_between_code_and_line_comments", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.format_line_comment_starting_on_first_column", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.indent_root_tags", (f, v) -> f.indent_root_tags = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_javadoc_comment", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_javadoc_comments", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.line_length", (f, v) -> f.comment_line_length = Integer.parseInt(v));
        settings_handlers.put("org.eclipse.jdt.core.formatter.join_lines_in_comments", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_html", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_source_code", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_header", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.format_block_comments", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.never_indent_block_comments_on_first_column", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.comment.new_lines_at_javadoc_boundaries", (f, v) -> f.new_lines_at_javadoc_boundaries = v.equals("true"));
        settings_handlers.put("org.eclipse.jdt.core.formatter.never_indent_line_comments_on_first_column", noop);

        settings_handlers.put("org.eclipse.jdt.core.formatter.use_on_off_tags", noop);
        settings_handlers.put("org.eclipse.jdt.core.formatter.enabling_tag", (f, v) -> f.enabling_tag = v);
        settings_handlers.put("org.eclipse.jdt.core.formatter.disabling_tag", (f, v) -> f.disabling_tag = v);
        // @formatter:on
    }

    private EclipseFormatLoader() {
    }

    @Override
    public void load(EmitterFormat format, Path formatter, Path import_order) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(import_order.toFile()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=");
                int part;
                String path;
                if (parts.length == 1) {
                    part = Integer.parseInt(parts[0]);
                    path = "";
                } else if (parts.length == 2) {
                    part = Integer.parseInt(parts[0]);
                    path = parts[1];
                } else {
                    System.err.println("Malformed importorder file");
                    continue;
                }
                if (part < format.import_order.size()) {
                    format.import_order.set(part, path);
                } else {
                    while (part > format.import_order.size()) {
                        format.import_order.add(null);
                    }
                    format.import_order.add(path);
                }
            }
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(formatter.toFile());
            doc.getDocumentElement().normalize();
            Element profiles = (Element) doc.getElementsByTagName("profiles").item(0);
            Element profile = (Element) profiles.getElementsByTagName("profile").item(0);
            NodeList settings = profile.getElementsByTagName("setting");
            for (int i = 0; i < settings.getLength(); i++) {
                Element setting = (Element) settings.item(i);
                String id = setting.getAttribute("id");
                String value = setting.getAttribute("value");
                BiConsumer<EmitterFormat, String> handler = settings_handlers.get(id);
                if (handler == null) {
                    continue;
                }
                handler.accept(format, value);
            }

        } catch (Exception e) {
            System.err.println("Error loading formatter xml:");
            e.printStackTrace();
        }
    }

}
