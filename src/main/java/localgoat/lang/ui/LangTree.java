package localgoat.lang.ui;

import localgoat.lang.compiler.token.StringToken;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;
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

	private static abstract class TokenWrapper<T extends Token> implements TreeNode{
		protected final T wrapped;

		public TokenWrapper(T wrapped){
			this.wrapped = wrapped;
		}

		@Override
		public TreeNode getParent(){
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(){
			return wrapped.value();
		}
	}

	private static class TokenLeafWrapper extends TokenWrapper<Token>{

		public TokenLeafWrapper(Token wrapped){
			super(wrapped);
		}

		@Override
		public TreeNode getChildAt(int childIndex){
			throw new UnsupportedOperationException();
		}

		@Override
		public int getChildCount(){
			throw new UnsupportedOperationException();
		}

		@Override
		public int getIndex(TreeNode node){
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getAllowsChildren(){
			return false;
		}

		@Override
		public boolean isLeaf(){
			return true;
		}

		@Override
		public Enumeration<? extends TreeNode> children(){
			throw new UnsupportedOperationException();
		}
	}

	private static class TokenTreeWrapper extends TokenWrapper<TokenTree>{

		TokenTreeWrapper(TokenTree wrapped){
			super(wrapped);
		}

		@Override
		public TreeNode getChildAt(int childIndex){
			final var child = wrapped.children().get(childIndex);
			if((child instanceof TokenTree) && !(child instanceof TokenSeries)){
				return new TokenTreeWrapper((TokenTree)child);
			}
			return new TokenLeafWrapper(child);
		}

		@Override
		public int getChildCount(){
			return wrapped.children().size();
		}

		@Override
		public int getIndex(TreeNode node){
			return wrapped.children().indexOf(((TokenWrapper)node).wrapped);
		}

		@Override
		public boolean getAllowsChildren(){
			return true;
		}

		@Override
		public boolean isLeaf(){
			return false;
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
			return String.valueOf(wrapped.head());
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
