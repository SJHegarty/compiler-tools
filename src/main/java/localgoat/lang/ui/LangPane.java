package localgoat.lang.ui;

import localgoat.lang.struct.CodeTree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

public class LangPane extends JTextPane{

	public LangPane(){
		getDocument().addDocumentListener(
			(StrippedListener)() -> {
				var code = new CodeTree(getText());
				class CodeTreeWrapper implements TreeNode{
					private final CodeTree wrapped;

					CodeTreeWrapper(CodeTree wrapped){
						this.wrapped = wrapped;
					}

					@Override
					public TreeNode getChildAt(int childIndex){
						return new CodeTreeWrapper(wrapped.children.get(childIndex));
					}

					@Override
					public int getChildCount(){
						return wrapped.children.size();
					}

					@Override
					public TreeNode getParent(){
						return new CodeTreeWrapper(wrapped.parent);
					}

					@Override
					public int getIndex(TreeNode node){
						return wrapped.children.indexOf(((CodeTreeWrapper)node).wrapped);
					}

					@Override
					public boolean getAllowsChildren(){
						return true;
					}

					@Override
					public boolean isLeaf(){
						return wrapped.children.isEmpty();
					}

					@Override
					public Enumeration<? extends TreeNode> children(){
						return Collections.enumeration(
							wrapped.children.stream()
								.map(child -> new CodeTreeWrapper(child))
								.collect(Collectors.toList())
						);
					}

					@Override
					public String toString(){
						return wrapped.head == null ? null : wrapped.head.content;
					}

					@Override
					public int hashCode(){
						return wrapped.hashCode() ^ 0xabcdef12;
					}

					@Override
					public boolean equals(Object o){
						return (o == this) || ((o instanceof CodeTreeWrapper) && ((CodeTreeWrapper)o).wrapped.equals(wrapped));
					}
				}
				tree.setModel(
					new DefaultTreeModel(new CodeTreeWrapper(code))
				);
			}
		);	}
}