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

import com.google.common.collect.Lists;
import org.spongepowered.despector.config.ConfigBase.FormatterConfig;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Configuration for the formatting of a source emitter.
 */
public class EmitterFormat {

    // ========================================================================
    // general
    // ========================================================================
    // max length of a line
    public int line_split = 999;
    // size of a tab in spaces
    public int indentation_size = 4;
    // number of times indented when a line is continued
    public int continuation_indentation = 2;
    // use spaces over tabs
    public boolean indent_with_spaces = true;
    // whether empty lines should be indented
    public boolean indent_empty_lines = false;
    // whether to insert a newline at end of file
    public boolean insert_new_line_at_end_of_file_if_missing = true;

    // ========================================================================
    // imports
    // ========================================================================
    public final List<String> import_order = Lists.newArrayList();

    public int blank_lines_before_package = 0;
    public int blank_lines_after_package = 1;
    public int blank_lines_before_imports = 1;
    public int blank_lines_after_imports = 1;
    public int blank_lines_between_import_groups = 1;

    // ========================================================================
    // parameterized types
    // ========================================================================

    // ========================================================================
    // class
    // ========================================================================
    public boolean insert_space_before_comma_in_superinterfaces = false;
    public boolean insert_space_after_comma_in_superinterfaces = true;

    public int blank_lines_before_first_class_body_declaration = 1;
    public BracePosition brace_position_for_type_declaration = BracePosition.SAME_LINE;
    public WrappingStyle alignment_for_superclass_in_type_declaration = WrappingStyle.DO_NOT_WRAP;
    public WrappingStyle alignment_for_superinterfaces_in_type_declaration = WrappingStyle.WRAP_WHEN_NEEDED;

    // ========================================================================
    // enum
    // ========================================================================
    public boolean insert_space_before_opening_brace_in_enum_declaration = true;
    public boolean insert_space_before_comma_in_enum_constant_arguments = false;
    public boolean insert_space_after_comma_in_enum_constant_arguments = true;
    public boolean insert_space_before_comma_in_enum_declarations = false;
    public boolean insert_space_after_comma_in_enum_declarations = true;
    public boolean insert_space_before_opening_paren_in_enum_constant = false;
    public boolean insert_space_after_opening_paren_in_enum_constant = false;
    public boolean insert_space_before_closing_paren_in_enum_constant = false;
    public boolean indent_body_declarations_compare_to_enum_declaration_header = true;
    public BracePosition brace_position_for_enum_declaration = BracePosition.SAME_LINE;
//    public WrappingStyle alignment_for_arguments_in_enum_constant = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_superinterfaces_in_enum_declaration = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_enum_constants = WrappingStyle.WRAP_ALL;

    // ========================================================================
    // field decl
    // ========================================================================
    public boolean align_type_members_on_columns = false;

    // ========================================================================
    // method decl
    // ========================================================================
    public boolean insert_space_between_empty_parens_in_method_declaration = false;
    public boolean insert_space_before_comma_in_method_invocation_arguments = false;
    public boolean insert_space_after_comma_in_method_invocation_arguments = true;
    public boolean insert_new_line_in_empty_method_body = false;
    public boolean insert_space_before_comma_in_method_declaration_throws = false;
    public boolean insert_space_after_comma_in_method_declaration_throws = true;
    public boolean insert_space_before_comma_in_method_declaration_parameters = false;
    public boolean insert_space_after_comma_in_method_declaration_parameters = true;
    public boolean insert_space_before_opening_paren_in_method_declaration = false;
    public boolean insert_space_before_closing_paren_in_method_declaration = false;
    public boolean insert_space_before_opening_brace_in_method_declaration = true;
    public BracePosition brace_position_for_method_declaration = BracePosition.SAME_LINE;
    public WrappingStyle alignment_for_parameters_in_method_declaration = WrappingStyle.WRAP_WHEN_NEEDED;

    // ========================================================================
    // ctor
    // ========================================================================

    public boolean insert_space_before_comma_in_explicitconstructorcall_arguments = false;
    public boolean insert_space_after_comma_in_explicitconstructorcall_arguments = true;
    public boolean insert_space_before_opening_paren_in_constructor_declaration = false;
    public boolean insert_space_after_opening_paren_in_constructor_declaration = false;
    public boolean insert_space_before_comma_in_constructor_declaration_parameters = false;
    public boolean insert_space_after_comma_in_constructor_declaration_parameters = true;
    public boolean insert_space_before_opening_brace_in_constructor_declaration = true;
    public boolean insert_space_after_comma_in_constructor_declaration_throws = true;
    public BracePosition brace_position_for_constructor_declaration = BracePosition.SAME_LINE;
    // explicit ctors are things like super(a,b); or this(foo,bar);
    public WrappingStyle alignment_for_arguments_in_explicit_constructor_call = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_parameters_in_constructor_declaration = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_throws_clause_in_constructor_declaration = WrappingStyle.WRAP_WHEN_NEEDED;

    // ========================================================================
    // try
    // ========================================================================

    public WrappingStyle alignment_for_union_type_in_multicatch = WrappingStyle.WRAP_WHEN_NEEDED;

    // ========================================================================
    // loops
    // ========================================================================

    public boolean new_lines_at_block_boundaries = true;

    // ========================================================================
    // ifs
    // ========================================================================

    // ========================================================================
    // switch
    // ========================================================================

    public boolean insert_space_after_opening_paren_in_switch = false;
    public boolean insert_space_before_closing_paren_in_switch = false;
    public boolean insert_space_before_colon_in_case = false;
    public boolean insert_space_after_colon_in_case = false;
    public boolean indent_breaks_compare_to_cases = true;
    public BracePosition brace_position_for_switch = BracePosition.SAME_LINE;

    // ========================================================================
    // annotations
    // ========================================================================

    // ========================================================================
    // comments
    // ========================================================================

    // ========================================================================
    // arrays
    // ========================================================================

    public boolean insert_space_between_brackets_in_array_type_reference = false;
    public boolean insert_space_after_opening_brace_in_array_initializer = false;
    public boolean insert_new_line_before_closing_brace_in_array_initializer = false;
    public WrappingStyle alignment_for_expressions_in_array_initializer = WrappingStyle.WRAP_WHEN_NEEDED;
    public BracePosition brace_position_for_array_initializer = BracePosition.SAME_LINE;
    public boolean insert_space_after_opening_bracket_in_array_allocation_expression = false;
    public boolean insert_space_before_closing_bracket_in_array_allocation_expression = false;
    public boolean insert_space_before_comma_in_array_initializer = false;
    public boolean insert_space_after_comma_in_array_initializer = true;
    public boolean insert_space_before_opening_bracket_in_array_type_reference = false;
    public boolean insert_new_line_after_opening_brace_in_array_initializer = false;
    public boolean insert_space_before_opening_bracket_in_array_reference = false;

    // ========================================================================
    // misc
    // ========================================================================

    public boolean insert_space_after_unary_operator = false;
    public boolean insert_space_before_binary_operator = true;
    public boolean insert_space_after_binary_operator = true;

    // TODO need to finish sorting these...
    public boolean indent_switchstatements_compare_to_switch = true;
    public boolean insert_space_before_opening_brace_in_switch = true;
    public boolean insert_space_before_opening_paren_in_for = true;
    public boolean insert_space_after_opening_paren_in_for = false;
    public boolean insert_space_after_comma_in_for_increments = true;
    public boolean insert_space_before_colon_in_for = true;
    public boolean insert_space_before_opening_paren_in_while = true;
    public boolean insert_space_after_opening_paren_in_while = false;
    public boolean insert_new_line_before_else_in_if_statement = false;
    public boolean keep_else_statement_on_same_line = true;
    public boolean insert_space_before_opening_paren_in_if = true;
    public boolean insert_space_before_closing_paren_in_if = false;
    public boolean compact_else_if = false;
    public boolean insert_new_line_before_catch_in_try_statement = false;
    public boolean insert_new_line_before_finally_in_try_statement = false;
    public boolean insert_space_before_closing_paren_in_try = false;
    public boolean insert_space_after_opening_paren_in_try = false;
    public WrappingStyle alignment_for_conditional_expression = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_arguments_in_method_invocation = WrappingStyle.WRAP_WHEN_NEEDED;
    public WrappingStyle alignment_for_selector_in_method_invocation = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_after_question_in_conditional = true;
    public boolean insert_space_after_question_in_wildcard = false;
    public boolean insert_space_after_lambda_arrow = true;
    public boolean insert_space_after_opening_paren_in_parenthesized_expression = false;
    public WrappingStyle blank_before_alignment_for_assignmentjavadoc_tags = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_after_semicolon_in_try_resources = true;
    public boolean indent_statements_compare_to_body = true;
    public boolean wrap_outer_expressions_when_nested = true;
    public boolean insert_space_before_closing_paren_in_cast = false;
    public boolean format_guardian_clause_on_one_line = false;
    public boolean insert_space_after_colon_in_labeled_statement = true;
    public boolean insert_space_before_parenthesized_expression_in_return = true;
    public boolean insert_space_before_parenthesized_expression_in_throw = true;
    public boolean insert_space_before_ellipsis = false;
    public BracePosition brace_position_for_block = BracePosition.SAME_LINE;
    public boolean insert_space_before_comma_in_for_inits = false;
    public boolean wrap_before_or_operator_multicatch = true;
    public boolean insert_space_before_closing_bracket_in_array_reference = false;
    public boolean insert_space_before_comma_in_allocation_expression = false;
    public boolean insert_space_after_closing_brace_in_block = true;
    public boolean insert_space_before_comma_in_constructor_declaration_throws = false;
    public boolean insert_space_after_opening_paren_in_if = false;
    public boolean insert_space_after_assignment_operator = true;
    public boolean insert_space_before_assignment_operator = true;
    public boolean insert_space_after_opening_paren_in_synchronized = false;
    public boolean insert_space_after_closing_paren_in_cast = true;
    public BracePosition brace_position_for_block_in_case = BracePosition.SAME_LINE;
    public boolean insert_space_after_opening_paren_in_catch = false;
    public boolean insert_space_before_opening_paren_in_method_invocation = false;
    public boolean insert_space_after_opening_bracket_in_array_reference = false;
    public WrappingStyle alignment_for_arguments_in_qualified_allocation_expression = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_after_and_in_type_parameter = true;
    public boolean insert_space_between_empty_brackets_in_array_allocation_expression = false;
    public boolean keep_empty_array_initializer_on_one_line = false;
    public int continuation_indentation_for_array_initializer = 2;
    public WrappingStyle alignment_for_arguments_in_allocation_expression = WrappingStyle.WRAP_WHEN_NEEDED;
    public BracePosition brace_position_for_lambda_body = BracePosition.SAME_LINE;
    public boolean insert_space_after_opening_paren_in_cast = false;
    public boolean insert_space_before_unary_operator = false;
    public boolean keep_imple_if_on_one_line = false;
    public boolean insert_space_before_colon_in_labeled_statement = false;
    public boolean insert_space_after_colon_in_for = true;
    public WrappingStyle alignment_for_binary_expression = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_before_closing_paren_in_while = false;
    public boolean insert_space_before_opening_paren_in_try = true;
    public boolean put_empty_statement_on_new_line = true;
    public boolean insert_new_line_after_label = false;
    public boolean insert_space_between_empty_parens_in_method_invocation = false;
    public boolean insert_new_line_before_while_in_do_statement = false;
    public boolean insert_space_before_semicolon = false;
    public int number_of_blank_lines_at_beginning_of_method_body = 0;
    public boolean insert_space_before_colon_in_conditional = true;
    public boolean indent_body_declarations_compare_to_type_header = true;
    public boolean insert_space_before_opening_paren_in_annotation_type_member_declaration = false;
    public boolean wrap_before_binary_operator = true;
    public boolean insert_space_before_closing_paren_in_synchronized = false;
    public boolean indent_statements_compare_to_block = true;
    public boolean insert_space_before_question_in_conditional = true;
    public boolean insert_space_before_comma_in_multiple_field_declarations = false;
    public WrappingStyle alignment_for_compact_if = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_after_comma_in_for_inits = true;
    public boolean indent_switchstatements_compare_to_cases = true;
    public boolean insert_space_before_colon_in_default = false;
    public boolean insert_space_before_and_in_type_parameter = true;
    public boolean insert_space_between_empty_parens_in_constructor_declaration = false;
    public boolean insert_space_after_colon_in_assert = true;
    public WrappingStyle alignment_for_throws_clause_in_method_declaration = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_before_opening_bracket_in_array_allocation_expression = false;
    public boolean insert_new_line_in_empty_anonymous_type_declaration = true;
    public boolean insert_space_after_colon_in_conditional = true;
    public boolean insert_space_before_closing_paren_in_for = false;
    public boolean insert_space_before_postfix_operator = false;
    public boolean insert_space_before_opening_paren_in_synchronized = true;
    public boolean insert_space_after_comma_in_allocation_expression = true;
    public boolean insert_space_before_closing_brace_in_array_initializer = false;
    public WrappingStyle alignment_for_resources_in_try = WrappingStyle.WRAP_WHEN_NEEDED;
    public boolean insert_space_before_lambda_arrow = true;
    public boolean insert_new_line_in_empty_block = true;
    public boolean insert_space_before_closing_paren_in_parenthesized_expression = false;
    public boolean insert_space_before_opening_paren_in_parenthesized_expression = false;
    public boolean insert_space_before_closing_paren_in_catch = false;
    public boolean insert_space_before_comma_in_multiple_local_declarations = false;
    public boolean insert_space_before_opening_paren_in_switch = true;
    public boolean insert_space_before_comma_in_for_increments = false;
    public boolean insert_space_after_opening_paren_in_method_invocation = false;
    public boolean insert_space_before_colon_in_assert = true;
    public boolean insert_space_before_opening_brace_in_array_initializer = true;
    public boolean insert_space_between_empty_braces_in_array_initializer = false;
    public boolean insert_space_after_opening_paren_in_method_declaration = false;
    public boolean insert_space_before_semicolon_in_for = false;
    public boolean insert_space_before_opening_paren_in_catch = true;
    public boolean insert_space_before_opening_angle_bracket_in_parameterized_type_reference = false;
    public boolean insert_space_after_comma_in_multiple_field_declarations = true;
    public boolean insert_space_after_comma_in_multiple_local_declarations = true;
    public boolean indent_body_declarations_compare_to_enum_constant_header = true;
    public boolean insert_space_after_semicolon_in_for = true;
    public boolean insert_space_before_semicolon_in_try_resources = false;
    public boolean keep_then_statement_on_same_line = false;
    public BracePosition brace_position_for_anonymous_type_declaration = BracePosition.SAME_LINE;
    public boolean insert_space_before_opening_brace_in_anonymous_type_declaration = true;
    public boolean insert_space_before_question_in_wildcard = false;
    public boolean insert_space_before_opening_brace_in_type_declaration = true;
    public boolean insert_space_before_closing_angle_bracket_in_parameterized_type_reference = false;
    public boolean insert_space_before_comma_in_parameterized_type_reference = false;
    public boolean insert_space_after_opening_angle_bracket_in_parameterized_type_reference = false;
    public boolean insert_space_after_comma_in_parameterized_type_reference = true;
    public boolean insert_space_before_opening_angle_bracket_in_type_arguments = false;
    public boolean insert_space_before_comma_in_type_parameters = false;
    public boolean insert_space_after_comma_in_type_parameters = true;
    public boolean insert_space_before_opening_angle_bracket_in_type_parameters = false;
    public boolean insert_space_after_closing_angle_bracket_in_type_parameters = true;
    public boolean insert_space_after_opening_angle_bracket_in_type_parameters = false;
    public boolean insert_space_before_closing_angle_bracket_in_type_parameters = false;
    public boolean insert_space_after_comma_in_type_arguments = true;
    public boolean insert_space_before_closing_angle_bracket_in_type_arguments = false;
    public boolean insert_space_after_closing_angle_bracket_in_type_arguments = false;
    public boolean insert_space_before_comma_in_type_arguments = false;
    public boolean insert_space_after_opening_angle_bracket_in_type_arguments = false;
    public boolean insert_space_after_comma_in_annotation = true;
    public boolean insert_new_line_in_empty_annotation_declaration = true;
    public boolean insert_space_after_opening_paren_in_annotation = false;
    public boolean insert_space_between_empty_parens_in_annotation_type_member_declaration = false;
    public boolean insert_space_before_opening_brace_in_annotation_type_declaration = true;
    public boolean insert_space_after_at_in_annotation = false;
    public boolean insert_new_line_after_annotation_on_local_variable = true;
    public boolean insert_new_line_after_annotation_on_method = true;
    public BracePosition brace_position_for_annotation_type_declaration = BracePosition.SAME_LINE;
    public boolean insert_space_before_comma_in_annotation = false;
    public boolean insert_space_after_at_in_annotation_type_declaration = false;
    public boolean insert_new_line_after_annotation_on_field = false;
    public boolean insert_space_before_opening_paren_in_annotation = false;
    public boolean insert_space_before_at_in_annotation_type_declaration = true;
    public boolean insert_new_line_after_type_annotation = false;
    public boolean insert_new_line_after_annotation_on_type = true;
    public boolean insert_new_line_after_annotation_on_parameter = false;
    public boolean insert_new_line_after_annotation_on_package = true;
    public WrappingStyle alignment_for_arguments_in_annotation = WrappingStyle.DO_NOT_WRAP;
    public boolean indent_body_declarations_compare_to_annotation_declaration_header = true;
    public boolean insert_space_before_closing_paren_in_annotation = false;
    public boolean clear_blank_lines_in_block_comment = true;
    public boolean insert_new_line_before_root_tags = true;
    public boolean insert_new_line_for_parameter = false;
    public boolean indent_parameter_description = false;
    public boolean indent_root_tags = true;
    public int comment_line_length = 80;
    public boolean new_lines_at_javadoc_boundaries = true;

    public void loadFrom(FormatterConfig conf) {
        for (Field fld : FormatterConfig.class.getFields()) {
            if (fld.getType().getName().startsWith("org.spongepowered.despector.")) {
                Object insn;
                try {
                    insn = fld.get(conf);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    continue;
                }
                for (Field f : fld.getType().getFields()) {
                    Field c = null;
                    try {
                        c = EmitterFormat.class.getField(f.getName());
                    } catch (NoSuchFieldException | SecurityException e) {
                        continue;
                    }
                    if (c != null) {
                        try {
                            c.set(this, f.get(insn));
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                        }
                    }
                }
                continue;
            }
            Field c = null;
            try {
                c = EmitterFormat.class.getField(fld.getName());
            } catch (NoSuchFieldException | SecurityException e) {
                continue;
            }
            if (c != null) {
                try {
                    c.set(this, fld.get(conf));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                }
            }
        }
    }

    public static enum WrappingStyle {
        DO_NOT_WRAP,
        WRAP_WHEN_NEEDED,
        WRAP_FIRST_OR_NEEDED,
        WRAP_ALL,
        WRAP_ALL_AND_INDENT,
        WRAP_ALL_EXCEPT_FIRST;
    }

    public static enum BracePosition {
        SAME_LINE,
        NEXT_LINE,
        NEXT_LINE_SHIFTED,
        NEXT_LINE_ON_WRAP;
    }

    private static final EmitterFormat default_format = new EmitterFormat();

    public static EmitterFormat defaults() {
        return default_format;
    }

    static {
        default_format.import_order.add("/#");
        default_format.import_order.add("");
        default_format.import_order.add("java");
        default_format.import_order.add("javax");
    }

}
