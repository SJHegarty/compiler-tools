package localgoat.lang.ui;

import localgoat.lang.compiler.token.TokenSeries;
import localgoat.lang.compiler.token.TokenString;
import localgoat.lang.compiler.token.TokenTree;
import localgoat.util.ui.ListTreeWrapper;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class LangTree extends JTree{

	private static class TokenTreeWrapper implements TreeNode{
		private final TokenTree wrapped;

		TokenTreeWrapper(TokenTree wrapped){
			this.wrapped = wrapped;
		}

		@Override
		public TreeNode getChildAt(int childIndex){
			return new TokenTreeWrapper((TokenTree) wrapped.children().get(childIndex));
		}

		@Override
		public int getChildCount(){
			return isLeaf() ? 0 : wrapped.children().size();
		}

		@Override
		public TreeNode getParent(){
			throw new UnsupportedOperationException();
			//return new CodeTreeWrapper(wrapped.parent());
		}

		@Override
		public int getIndex(TreeNode node){
			return wrapped.children().indexOf(((TokenTreeWrapper)node).wrapped);
		}

		@Override
		public boolean getAllowsChildren(){
			return true;
		}

		@Override
		public boolean isLeaf(){
			return wrapped instanceof TokenString;
		}

		@Override
		public Enumeration<? extends TreeNode> children(){
			if(isLeaf()){
				throw new UnsupportedOperationException();
			}
			return Collections.enumeration(
				wrapped.children().stream()
					.map(child -> new TokenTreeWrapper((TokenTree) child))
					.collect(Collectors.toList())
			);
		}

		@Override
		public String toString(){
			return (isLeaf() || wrapped instanceof TokenSeries) ? wrapped.value() : String.valueOf(wrapped.head());
		}

		@Override
		public int hashCode(){
			return wrapped.hashCode() ^ 0xabcdef12;
		}

		@Override
		public boolean equals(Object o){
			return (o == this) || ((o instanceof TokenTreeWrapper) && ((TokenTreeWrapper)o).wrapped.equals(wrapped));
		}
	}

	public void setCodeTrees(List<TokenTree> trees){
		setModel(
			new DefaultTreeModel(
				new ListTreeWrapper<>(
					trees,
					tree -> new TokenTreeWrapper(tree)
				)
			)
		);
	}

	public void setCodeTree(TokenTree tree){
		setModel(new DefaultTreeModel(new TokenTreeWrapper(tree)));
	}
}
