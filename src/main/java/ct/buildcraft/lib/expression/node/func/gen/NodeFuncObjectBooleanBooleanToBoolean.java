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
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectBooleanBooleanToBoolean<A> extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncObjectBooleanBooleanToBoolean<A> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectBooleanBooleanToBoolean(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToBoolean<A> function) {
        this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectBooleanBooleanToBoolean(Class<A> argTypeA, IFuncObjectBooleanBooleanToBoolean<A> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncObjectBooleanBooleanToBoolean<A> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean c = stack.popBoolean();
        INodeBoolean b = stack.popBoolean();
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncObjectBooleanBooleanToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectBooleanBooleanToBoolean create(INodeObject<A> argA, INodeBoolean argB, INodeBoolean argC) {
        return new FuncObjectBooleanBooleanToBoolean(argA, argB, argC); 
    }

    public class FuncObjectBooleanBooleanToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeBoolean argB;
        public final INodeBoolean argC;

        public FuncObjectBooleanBooleanToBoolean(INodeObject<A> argA, INodeBoolean argB, INodeBoolean argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncObjectBooleanBooleanToBoolean(a, b, c),
                    (a, b, c) -> new FuncObjectBooleanBooleanToBoolean(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncObjectBooleanBooleanToBoolean(a, b, c),
                (a, b, c) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
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
            return NodeFuncObjectBooleanBooleanToBoolean.this;
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
            FuncObjectBooleanBooleanToBoolean other = (FuncObjectBooleanBooleanToBoolean) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectBooleanBooleanToBoolean<A> {
        boolean apply(A a, boolean b, boolean c);
    }
}
