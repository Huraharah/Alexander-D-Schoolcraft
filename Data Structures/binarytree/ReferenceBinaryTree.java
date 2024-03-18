import java.util.*;
public class ReferenceBinaryTree {
	Node root;
	int size;

	public ReferenceBinaryTree(){
		this.root=null;
	}

	public ReferenceBinaryTree(Node n){
		this.root=n;
	}

	public boolean addLeft(Node n, Integer e){//O(1)
		if(n.getLeft()==null){
			Node temp=new Node(e);
			temp.setParent(n);
			n.setLeft(temp);
			return true;
		}
		return false;
	}

	public boolean addRight(Node n, Integer e){//O(1)
		if(n.getRight()==null){
			Node temp=new Node(e);
			temp.setParent(n);
			n.setRight(temp);
			return true;
		}
		return false;
	}

	public void preOrder(Node n){//O(n)
		if(n!=null) System.out.print(n.getElement()+"\t");
		if(n.getLeft()!=null) preOrder(n.getLeft());
		if(n.getRight()!=null)preOrder(n.getRight());
	}

	public ArrayList preOrder(Node n, ArrayList list){
		if(n!=null) list.add(n.getElement());
		if(n.getLeft()!=null) preOrder(n.getLeft(), list);
		if(n.getRight()!=null)preOrder(n.getRight(), list);
		return list;
	}

	/*public boolean search(Node n, Integer target){
		boolean result1=false, result2=false;
		if(n!=null && n.getElement().equals(target)) return true;
		if(n.getLeft()!=null)  result1=search(n.getLeft(), target);
		if(n.getRight()!=null) result2= search(n.getRight(), target);
		return result1||result2 ;
	}*/

	public Node search(Node n, Integer target){
		Node result1=null, result2=null;
		if(n!=null && n.getElement().equals(target)) return n;
		if(n.getLeft()!=null)  result1=search(n.getLeft(), target);
		if(n.getRight()!=null) result2= search(n.getRight(), target);
		if(result1!=null)
			return result1;
		else if(result2!=null)
			return result2;
		return null;
	}

	public int size(Node n){//O(n)
		int count=0;
		if(n!=null) count++;
		if(n.getLeft()!=null) count=count+size(n.getLeft());
		if(n.getRight()!=null) count=count+size(n.getRight());

		return count;
	}

	public int numChild(Node n){//O(1)
		int count=0;
		if(n!=null&&n.getLeft()!=null) count++;
		if(n!=null&&n.getRight()!=null) count++;
		return count;
	}





























}


