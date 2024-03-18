public class Node {
	private Integer element;
	private Node parent;
	private Node left;
	private Node right;

	public Node(Integer element) {
		this.element = element;
		this.parent=null;
		this.left=null;
		this.right=null;
	}

	public Node(Integer element, Node parent, Node left, Node right) {
		this.element = element;
		this.parent = parent;
		this.left = left;
		this.right = right;
	}

	public Integer getElement() {
		return element;
	}

	public void setElement(Integer element) {
		this.element = element;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "[" + element +  "]";
	}







}
