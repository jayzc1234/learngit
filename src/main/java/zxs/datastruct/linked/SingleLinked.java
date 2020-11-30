package zxs.datastruct.linked;

public class SingleLinked<T> {
    private SingleLinkedNode<T> head;
    private int size;
    
	public SingleLinked() {
		head=new SingleLinkedNode<T>();
	}
    
	public void insert(T t) {
		SingleLinkedNode<T> first=head.next;
		SingleLinkedNode<T> newNode=new SingleLinkedNode<T>();
		newNode.data=t;
		if (null==first) {
			head.next=newNode;
		}else {
			SingleLinkedNode<T> lastNode=null;
			while(null!=first) {
				lastNode=first;
				first=first.next;
			}
			lastNode.next=newNode;
		}
		
		++size;
	}
	
	public void traverse() {
		SingleLinkedNode<T> first=head.next;
		
		while(null!=first) {
			T data=first.data;
			first=first.next;
			if (first==null) {
				System.out.println(data);
			}else {
				System.out.print(data+" , ");
			}
		}
	}
	
	public void delete(T t) {
		SingleLinkedNode<T> first=head.next;
		SingleLinkedNode<T> pre=head;
		while(null!=first) {
			T data=first.data;
			if (data.equals(t)) {
				System.out.println(data);
				pre.next=first.next;
				first.next=null;
				--size;
				break;
			}
			first=first.next;
			pre=first;
		}
	}
	
	public void reverse() {
		SingleLinkedNode<T> prenode=head;
		SingleLinkedNode<T> nextnode=null;
		SingleLinkedNode<T> currnode=head.next;
		SingleLinkedNode<T> reverseHead=null;
		head.next=null;
		while(null!=currnode) {
			nextnode=currnode.next;
			currnode.next=prenode;
			prenode=currnode;
			if (null==nextnode) {
				reverseHead=currnode;
				head=reverseHead;
			}
			currnode=nextnode;
		}
	}
	
	public int size() {
		return size;
	}
	
	public static void main(String[] args) {
		SingleLinked<Integer> list=new SingleLinked<Integer>();
		list.insert(1);
		list.insert(11);
		list.insert(12);
		list.insert(13);
		list.insert(14);
		list.traverse();
		System.out.println("-------------------------------");
		list.reverse();
		list.traverse();
//		System.out.println(list.size);
	}
	
	
}
