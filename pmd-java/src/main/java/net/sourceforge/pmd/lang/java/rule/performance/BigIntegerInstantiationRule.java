/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.performance;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTArrayDimsAndInits;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

/**
 * Rule that marks instantiations of new {@link BigInteger} or
 * {@link BigDecimal} objects, when there is a well-known constant available,
 * such as {@link BigInteger#ZERO}.
 */
public class BigIntegerInstantiationRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        Node type = node.getChild(0);

        if (!(type instanceof ASTClassOrInterfaceType)) {
            return super.visit(node, data);
        }

        boolean jdk15 = ((RuleContext) data).getLanguageVersion().compareToVersion("1.5") >= 0;
        boolean jdk9 = jdk15 && ((RuleContext) data).getLanguageVersion().compareToVersion("9") >= 0;
        if ((TypeTestUtil.isA(BigInteger.class, (ASTClassOrInterfaceType) type)
                || jdk15 && TypeTestUtil.isA(BigDecimal.class, (ASTClassOrInterfaceType) type))
                && !node.hasDescendantOfType(ASTArrayDimsAndInits.class)) {
            ASTArguments args = node.getFirstChildOfType(ASTArguments.class);
            if (args.size() == 1) {
                ASTLiteral literal = node.getFirstDescendantOfType(ASTLiteral.class);
                if (literal == null
                        || literal.getParent().getParent().getParent().getParent().getParent() != args) {
                    return super.visit(node, data);
                }

                String img = literal.getImage();
                if (literal.isStringLiteral()) {
                    img = img.substring(1, img.length() - 1);
                }

                if ("0".equals(img) || "1".equals(img) || jdk9 && "2".equals(img) || jdk15 && "10".equals(img)) {
                    addViolation(data, node);
                    return data;
                }
            }
        }
        return super.visit(node, data);
    }

}
