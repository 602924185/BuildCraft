/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.func.gen;

import java.util.Objects;

import ct.buildcraft.lib.expression.NodeInliningHelper;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleDoubleDoubleToObject<R> extends NodeFuncBase implements INodeFuncObject<R> {

    public final IFuncDoubleDoubleDoubleToObject<R> function;
    private final StringFunctionQuad stringFunction;
    private final Class<R> returnType;

    public NodeFuncDoubleDoubleDoubleToObject(String name, Class<R> returnType, IFuncDoubleDoubleDoubleToObject<R> function) {
        this(returnType, function, (a, b, c) -> "[ double, double, double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncDoubleDoubleDoubleToObject(Class<R> returnType, IFuncDoubleDoubleDoubleToObject<R> function, StringFunctionQuad stringFunction) {
        this.returnType = returnType;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public Class<R> getType() {
        return returnType;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncDoubleDoubleDoubleToObject<R> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble c = stack.popDouble();
        INodeDouble b = stack.popDouble();
        INodeDouble a = stack.popDouble();

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncDoubleDoubleDoubleToObject} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncDoubleDoubleDoubleToObject create(INodeDouble argA, INodeDouble argB, INodeDouble argC) {
        return new FuncDoubleDoubleDoubleToObject(argA, argB, argC); 
    }

    public class FuncDoubleDoubleDoubleToObject implements INodeObject<R>, IDependantNode, IFunctionNode {
        public final INodeDouble argA;
        public final INodeDouble argB;
        public final INodeDouble argC;

        public FuncDoubleDoubleDoubleToObject(INodeDouble argA, INodeDouble argB, INodeDouble argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncDoubleDoubleDoubleToObject(a, b, c),
                    (a, b, c) -> new FuncDoubleDoubleDoubleToObject(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncDoubleDoubleDoubleToObject(a, b, c),
                (a, b, c) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
        }

        @Override
        public void visitDependants(IDependancyVisitor visitor) {
            if (!canInline) {
                if (function instanceof IDependantNode) {
                    visitor.dependOn((IDependantNode) function);
                } else {
                    visitor.dependOnExplictly(this);
                }
            }
            visitor.dependOn(argA, argB, argC);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncDoubleDoubleDoubleToObject.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB, argC);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncDoubleDoubleDoubleToObject other = (FuncDoubleDoubleDoubleToObject) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleDoubleDoubleToObject<R> {
        R apply(double a, double b, double c);
    }
}
