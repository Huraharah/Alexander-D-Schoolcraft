public class ArrayBinaryTree {
	Integer[] bArray;

	public ArrayBinaryTree(int capacity){
		bArray=new Integer[capacity];
	}

	public void addRoot(Integer e){
		if(bArray[0]==null) bArray[0]=e;
	}
	public ArrayBinaryTree(int capacity, Integer root){
		bArray=new Integer[capacity];
		bArray[0]=root;
	}

	public boolean addLeft(int i, Integer e){//O(1)
		if(bArray[2*i+1]==null){
			bArray[2*i+1]=e;
			return true;
		}
		return false;
	}

	public boolean addRight(int i, Integer e){//O(1)
		if(bArray[2*i+2]==null){
			bArray[2*i+2]=e;
			return true;
		}
		return false;
	}
	/*
	 * b.addLeft(0, 3);
		b.addRight(0, 4);
		b.addLeft(1, 1);
		b.addRight(2, 2);
		b.preOrder(0);
	 */

	/*public void preOrder(Node n){//O(n)
		if(n!=null) System.out.print(n.getElement()+"\t");
		if(n.getLeft()!=null) preOrder(n.getLeft());
		if(n.getRight()!=null)preOrder(n.getRight());
	}*/
	public void preOrder(int i){//O(n)
		if(bArray[i]!=null) System.out.print(bArray[i]+"\t");
		if(bArray[2*i+1]!=null) preOrder(2*i+1);
		if(bArray[2*i+2]!=null) preOrder(2*i+2);
	}









}
