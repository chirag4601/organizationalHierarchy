import java.util.*;

// Tree node
class Node {

	//fields for general tree
	int id;
	int level;
	Node parent;
	Vector<Node> children;

	//fields for AVL tree
	int height;
	Node leftChild;
	Node rightChild;

	public Node(int id){
		children = new Vector<>();
		this.id = id;
	}
}

public class OrgHierarchy implements OrgHierarchyInterface {

	//root nodes for both trees
	public Node root;
	public Node owner;
	int size = 0;

	//checks if trees are empty
	public boolean isEmpty() {
		return size() == 0;
	}

	//returns size of the trees
	public int size() {
		return size;
	}

	public int level(int id) throws IllegalIDException, EmptyTreeException {	//O(logn)
		if (isEmpty()) {
			throw new EmptyTreeException("No IDs present");
		}
		if (owner.id == id) {
			return owner.level;
		} else {
			Node ID = find(id);
			if (ID == null) {
				throw new IllegalIDException("ID not found");
			}
			return ID.level;
		}
	}

	public void hireOwner(int id) throws NotEmptyException {	//O(1)
		if (isEmpty()) {

			//adds root in AVL tree
			root = insertAVL(id,root);

			//adds owner for the organisation
			owner = new Node(id);
			owner.level = 1;
			owner.parent = null;
			size++;

		} else
			throw new NotEmptyException("Owner already present");
	}

	public void hireEmployee(int id, int bossid) throws IllegalIDException, EmptyTreeException {	//O(logn)
		if (isEmpty())
			throw new EmptyTreeException("Database is empty");

		if(find(id)==null)
			root = insertAVL(id,root);
		else{
			if(find(id).parent!=null)
				throw new IllegalIDException("ID already exists");
		}

		Node node = find(id);

		Node boss = find(bossid);
		if(bossid == owner.id)
			boss = owner;

		if (boss == null)
			throw new IllegalIDException("Boss Id not found");

		boss.children.add(node);
		node.parent = boss;
		node.level = boss.level + 1;
		size++;
	}

	public void fireEmployee(int id) throws IllegalIDException, EmptyTreeException {	//O(logn)
		if (isEmpty())
			throw new EmptyTreeException("No employee present");
		if (owner.id == id)
			throw new IllegalIDException("Cannot fire owner");

		if(!find(id).children.isEmpty()){
			throw new IllegalIDException("No manage id to relocate the employee");
		}
		Node employee = find(id);
		if (employee == null)
			throw new IllegalIDException("Employee not present");


		employee.parent.children.removeElement(employee);
		employee.parent = null;
		size--;
	}

	public void fireEmployee(int id, int manageid) throws IllegalIDException, EmptyTreeException {	//O(n)
		if (owner.id == id)
			throw new IllegalIDException("Cannot fire owner");
		if (isEmpty())
			throw new EmptyTreeException("No employee present");

		Node employee = find(id);
		Node newEmployee = find(manageid);

		if (employee == null || newEmployee == null)
			throw new IllegalIDException("Employee not present");

		for (Node n : employee.children) {
			n.parent = newEmployee;
			newEmployee.children.add(n);

		}

		employee.parent.children.removeElement(employee);
		employee.parent = null;
		employee.children.clear();
		size--;
	}

	public int boss(int id) throws IllegalIDException, EmptyTreeException {	//O(logn)
		if (isEmpty())
			throw new EmptyTreeException("No employee present");
		if (owner.id == id)
			return -1;

		Node employee = find(id);
		if (employee == null)
			throw new IllegalIDException("Employee not present");

		return employee.parent.id;
	}

	public int lowestCommonBoss(int id1, int id2) throws IllegalIDException, EmptyTreeException {	//O(logn)
		if (isEmpty())
			throw new EmptyTreeException("No employee present");
		if (owner.id == id1 || owner.id == id2)
			return -1;

		Node employee1 = find(id1);
		Node employee2 = find(id2);
		if (employee1 == null || employee2 == null)
			throw new IllegalIDException("IDs not present");

		Vector<Node> boss1 = new Vector<>();
		Vector<Node> boss2 = new Vector<>();

		while (employee1.parent != null) {
			boss1.add(employee1.parent);
			employee1 = employee1.parent;
		}
		while (employee2.parent != null) {
			boss2.add(employee2.parent);
			employee2 = employee2.parent;
		}

		for (Node n : boss1) {
			if (boss2.contains(n)) {
				return n.id;
			}
		}
		return 0;
	}

	public String toString(int id) throws IllegalIDException, EmptyTreeException {	//O(n^2)
		if (isEmpty())
			throw new EmptyTreeException("Tree is Empty");

		Node node = find(id);
		if(id==owner.id)
			node = owner;

		if (node == null)
			throw new IllegalIDException("ID not found");

		String s = new String();
		s = s + id + ",";
		int temp = node.level + 1;
		Vector<Integer> levels;

		do {
			levels = findLevel(temp, node);
			if (!levels.isEmpty()) {
				for (int i = 0; i < levels.size() - 1; i++) {
					s = s + levels.get(i) + " ";
				}
				s = s + levels.lastElement() + ",";
				temp++;
			}
		} while (!levels.isEmpty());

		return s.substring(0, s.length() - 1);

	}

	private Node find(int id) { //O(logn)
		Node current = root;

		while (current != null) {
			if (id < current.id)
				current = current.leftChild;
			else if (id > current.id)
				current = current.rightChild;
			else
				return current;
		}
		return null;
	}

	private Vector<Integer> findLevel(int level, Node node) {	//O(n)
		Vector<Integer> v = new Vector<>();

		for(Node n: node.children){
			if(n.level==level)
				v.add(n.id);
			else{
				Vector temp = findLevel(level, n);
				v.addAll(temp);
			}
		}

		Collections.sort(v);
		return v;
	}

	private int height(Node node) {	//O(1)
		if (node == null)
			return -1;
		return node.height;
	}

	private Node insertAVL(int id, Node node) {	//O(logn)
		if (node == null)
			node = new Node(id);

		else if (id < node.id)
			node.leftChild = insertAVL(id, node.leftChild);
		else
			node.rightChild = insertAVL(id, node.rightChild);

		node.height = Math.max(height(node.leftChild), height(node.rightChild)) + 1;

		return balance(node);
	}

	private boolean isLeftHeavy(Node node){	//O(1)
		return balanceFactor(node) > 1;
	}

	private boolean isRightHeavy(Node node){	//O(1)
		return balanceFactor(node) < -1;
	}

	private int balanceFactor(Node node){	//O(1)
		if(node==null)
			return 0;
		return height(node.leftChild) - height(node.rightChild);
	}

	private Node balance(Node node){	//O(1)
		if(isLeftHeavy(node)){
			if(balanceFactor(node.leftChild)<0)
				node.leftChild = rotateLeft(node.leftChild);
			return rotateRight(node);
		}
		else if(isRightHeavy(node)){
			if(balanceFactor(node.rightChild)>0)
				node.rightChild = rotateRight(node.rightChild);
			return rotateLeft(node);
		}

		return node;
	}

	private Node rotateLeft(Node node){	//O(1)
		Node newNode = node.rightChild;

		node.rightChild = newNode.leftChild;
		newNode.leftChild = node;

		node.height = Math.max(height(node.leftChild),height((node.rightChild)))+1;
		newNode.height = Math.max(height(newNode.leftChild),height((newNode.rightChild)))+1;
		return newNode;
	}

	private Node rotateRight(Node node){	//O(1)
		Node newNode = node.leftChild;

		node.leftChild = newNode.rightChild;
		newNode.rightChild = node;

		node.height = Math.max(height(node.leftChild),height((node.rightChild)))+1;
		newNode.height = Math.max(height(newNode.leftChild),height((newNode.rightChild)))+1;
		return newNode;
	}
}


