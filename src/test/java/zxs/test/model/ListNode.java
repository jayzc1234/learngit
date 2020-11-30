package zxs.test.model;

import lombok.Data;

@Data
public class ListNode {
    public int val;
    public ListNode next;
    public ListNode(int val){
        this.val=val;
    }


    /**
     * 根据数组生成单链表
     * @param ts
     * @param 
     * @return
     */
    public static ListNode addNodes(int[] ts){
        ListNode ListNode=new ListNode(0);
        ListNode tmp=ListNode;
        for (int t1 : ts) {
            ListNode newNode=new ListNode(t1);
            tmp.next=newNode;
            tmp=newNode;
        }
        return ListNode.next;
    }

    /**
     * 根据数组生成单链表
     * @param ts
     * @param 
     * @return
     */
    public static ListNode addNode(int ts,ListNode ListNode){
        if (null == ListNode){
            ListNode=new  ListNode(ts);
        }else {
            ListNode tmp=ListNode;
            while (null != tmp.next){
                tmp=tmp.next;
            }
            tmp.next=new ListNode(ts);
        }
        return ListNode;
    }


    public static void main(String[] args) {
    }

    /**
     * 高效反转单链表
     * @param ListNode
     */
    private static void reverse(ListNode ListNode)  {

        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
