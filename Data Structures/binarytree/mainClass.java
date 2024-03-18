import java.util.*;
public class mainClass {

	public static void main(String[] args) {
		ArrayBinaryTree b= new ArrayBinaryTree(100, 5);
		b.addLeft(0, 3);
		b.addRight(0, 4);
		b.addLeft(1, 1);
		b.addRight(2, 2);
		b.preOrder(0);

		/*Node n1 = new Node(5);
		ReferenceBinaryTree b=new ReferenceBinaryTree(n1);
		//System.out.println(b.root);
		b.addLeft(b.root, 3);
		b.addRight(b.root, 4);
		b.addLeft(b.root.getLeft(), 1);
		b.addRight(b.root.getRight(), 2);
		//b.preOrder(b.root);
		//System.out.println();
		//System.out.println(b.size(b.root));
		System.out.println(b.search(b.root, 3));

		ArrayList<Integer> list=new ArrayList<Integer>();
		list=b.preOrder(b.root, list);

		for(Integer i:list)
			System.out.print(i+"\t");*/














	}

}
