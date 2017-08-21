package com.puppycrawl.tools.checkstyle;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * @author LuoLiangchen
 */
public class UnitTestProcessorCheck extends AbstractCheck {
    @Override
    public int[] getDefaultTokens() {
        return new int[]{
            TokenTypes.ANNOTATION,
        };
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{
            TokenTypes.ANNOTATION,
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{
            TokenTypes.ANNOTATION,
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        if ("Test".equals(ast.findFirstToken(TokenTypes.IDENT).getText())) {
            final DetailAST methodDef = ast.getParent().getParent();
            final String methodName = methodDef.findFirstToken(TokenTypes.IDENT).getText();
            final DetailAST methodBlock = methodDef.findFirstToken(TokenTypes.SLIST);
            final Optional<String> configVariableName =
                    getModuleConfigVariableName(methodBlock);
            if (configVariableName.isPresent()) {
                for (DetailAST expr : getAllChildrenWithToken(methodBlock, TokenTypes.EXPR)) {
                    if (isAddAttributeMethodCall(expr.getFirstChild(), configVariableName.get())) {
                        final DetailAST elist = expr.getFirstChild().findFirstToken(TokenTypes.ELIST);
                        final String key = convertExprToText(elist.getFirstChild());
                        final String value = convertExprToText(elist.getLastChild());
                    }
                }
            }
        }
    }

    private Optional<String> getModuleConfigVariableName(DetailAST methodBlock) {
        Optional<String> returnValue = Optional.empty();

        for (DetailAST ast = methodBlock.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.VARIABLE_DEF) {
                final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
                final DetailAST assign = ast.findFirstToken(TokenTypes.ASSIGN);
                if (isDefaultConfigurationType(type) && isCreateModuleConfigAssign(assign)) {
                    returnValue = Optional.of(type.getNextSibling().getText());
                    break;
                }
            }
        }

        return returnValue;
    }

    private boolean isDefaultConfigurationType(DetailAST ast) {
        return "DefaultConfiguration".equals(ast.getFirstChild().getText());
    }

    private boolean isCreateModuleConfigAssign(DetailAST ast) {
        final boolean result;

        if (ast == null) {
            result = false;
        }
        else {
            final DetailAST exprChild = ast.getFirstChild().getFirstChild();
            result = exprChild.getType() == TokenTypes.METHOD_CALL
                    && exprChild.getFirstChild().getType() == TokenTypes.IDENT
                    && "createModuleConfig".equals(exprChild.getFirstChild().getText());
        }

        return result;
    }

    private List<DetailAST> getAllChildrenWithToken(DetailAST parent, int type) {
        final List<DetailAST> returnValue = new LinkedList<>();
        for (DetailAST ast = parent.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == type) {
                returnValue.add(ast);
                break;
            }
        }
        return returnValue;
    }

    private boolean isAddAttributeMethodCall(DetailAST ast, String variableName) {
        final boolean result;

        if (ast.getType() == TokenTypes.METHOD_CALL
                && ast.getFirstChild().getType() == TokenTypes.DOT) {
            final DetailAST dot = ast.getFirstChild();
            result = variableName.equals(dot.getFirstChild().getText())
                    && "addAttribute".equals(dot.getLastChild().getText());
        }
        else {
            result = false;
        }

        return result;
    }

    private String convertExprToText(DetailAST ast) {
        final String original = ast.getFirstChild().getText();
        return original.substring(1, original.length() - 1);
    }
}
