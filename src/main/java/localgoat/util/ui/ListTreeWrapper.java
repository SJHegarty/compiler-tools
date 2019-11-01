package localgoat.util.ui;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListTreeWrapper<T> implements TreeNode{

	private final List<T> children;
	private final Function<T, TreeNode> wrapperer;

	public ListTreeWrapper(List<T> children, Function<T, TreeNode> wrapperer){
		this.children = children;
		this.wrapperer = wrapperer;
	}

	@Override
	public TreeNode getChildAt(int childIndex){
		return wrapperer.apply(children.get(childIndex));
	}

	@Override
	public int getChildCount(){
		return children.size();
	}

	@Override
	public TreeNode getParent(){
		return null;
	}

	@Override
	public int getIndex(TreeNode node){
		for(int i = 0; i < children.size(); i++){
			if(wrapperer.apply(children.get(i)).equals(node)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren(){
		return true;
	}

	@Override
	public boolean isLeaf(){
		return children.isEmpty();
	}

	@Override
	public Enumeration<? extends TreeNode> children(){
		return Collections.enumeration(
			children.stream()
				.map(child -> wrapperer.apply(child))
				.collect(Collectors.toList())
		);
	}
}
