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
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongLongToLong extends NodeFuncBase implements INodeFuncLong {

    public final IFuncLongLongToLong function;
    private final StringFunctionTri stringFunction;

    public NodeFuncLongLongToLong(String name, IFuncLongLongToLong function) {
        this(function, (a, b) -> "[ long, long -> long ] " + name + "(" + a + ", " + b +  ")");
    }

    public NodeFuncLongLongToLong(IFuncLongLongToLong function, StringFunctionTri stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public NodeFuncLongLongToLong setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong b = stack.popLong();
        INodeLong a = stack.popLong();

        return create(a, b);
    }

    /** Shortcut to create a new {@link FuncLongLongToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncLongLongToLong create(INodeLong argA, INodeLong argB) {
        return new FuncLongLongToLong(argA, argB); 
    }

    public class FuncLongLongToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeLong argA;
        public final INodeLong argB;

        public FuncLongLongToLong(INodeLong argA, INodeLong argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB,
                    (a, b) -> new FuncLongLongToLong(a, b),
                    (a, b) -> new FuncLongLongToLong(a, b)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB,
                (a, b) -> new FuncLongLongToLong(a, b),
                (a, b) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate()))
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
            visitor.dependOn(argA, argB);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncLongLongToLong.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncLongLongToLong other = (FuncLongLongToLong) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB);
        }
    }

    @FunctionalInterface
    public interface IFuncLongLongToLong {
        long apply(long a, long b);
    }
}
