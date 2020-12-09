package com.zxs.test.other;

import com.zxs.test.model.ListNode;
import com.zxs.test.model.OneWayNode;

import java.util.concurrent.atomic.AtomicInteger;

public class ListNodeTest {
    private static ListNode l1;
    private static ListNode l2;

    static {
        l1=new ListNode(2);
        l1.next=new ListNode(4);
        l1.next.next=new ListNode(3);

        l2=new ListNode(5);
        l2.next=new ListNode(6);
        l2.next.next=new ListNode(4);
    }

    public static void main(String[] args) {
        AtomicInteger count = new AtomicInteger();
        ListNode listNode = ListNode.addNodes(new int[]{0,1, 2, 3, 4, 5});
        ListNode listNode1 = middleNode(listNode);
        System.out.println(listNode1);
    }

    public static  ListNode middleNode(ListNode head) {
        ListNode tmp=head;
        int count=0;
        while(null != tmp.next){
            count++;
            tmp=tmp.next;
        }

        int index=(count)/2;
        if(index ==0){
            return head.next;
        }else{
            count=0;
            tmp=head;
            while(null != tmp.next){
                if(count == index){
                    return tmp.next;
                }
                tmp=tmp.next;
                count++;
            }
        }
        return null;
    }

    public static void swap() {
        ListNode listNode = ListNode.addNodes(new int[]{1,2,3,4});
        ListNode listNode1 = swapPairs(listNode);
        System.out.println(listNode1);
    }
    public static ListNode swapPairs(ListNode head) {
        ListNode first=head;
        ListNode newNode=null;
        ListNode tmp=null;
        ListNode third=null;
        boolean init=true;
        while(null != first){
            ListNode second=first.next;
            if(null != second){
                third=second.next;

                if(null == newNode){
                    newNode=second;
                    tmp=newNode;
                    tmp.next=first;
                    tmp=first;
                    tmp.next=null;
                }else{
                    tmp.next=second;
                    tmp=first;
                    second.next=first;
                    tmp.next=null;
                }
                first=third;
            }else{
                if(null == newNode){
                    newNode=first;
                }else{
                    tmp.next=first;
                    tmp=first;
                    tmp.next=null;
                }
                break;
            }

        }
        return newNode;
    }
    public static ListNode addTwoNumbers() {
        ListNode ln=null;
        int carray=0;
        ListNode tmp=null;
        while((null!=l1) || (null != l2)){
            int newVal = getNewVal(carray);
            ListNode ln2;
            if(newVal>=10){
                //更新上一个节点值
                carray=(newVal) / 10;
                newVal=(newVal) % 10;
            }else {
                carray=0;
            }
            ln2=new ListNode(newVal);
            if(null==ln){
                ln=ln2;
            }else{
                tmp=ln;
                while (null !=tmp.next){
                    tmp=tmp.next;
                }
                tmp.next=ln2;
            }
            l1=l1.next;
            l2=l2.next;
        }
        if(carray>0){
            tmp.next=new ListNode(carray);
        }
        return ln;
    }

    private static int getNewVal(int carray) {
        int newVal=0;
        ListNode ln2=null;
        if(null==l1){
            newVal=l2.val+carray;
        }else if(null==l2){
            newVal=l1.val+carray;
        }else{
            newVal=l1.val+l2.val+carray;
        }
        return newVal;
    }


    /**
     * 遍历所有数组元素，如果元素不为空则记录下当前元素的值
     * 一轮下来获取到当前最小的值，获取到最小值所对应的ListNode使其向后移一位
     * @return
     */
    public static OneWayNode<Integer> mergeKLists(OneWayNode[] lists) {
        int length=lists.length;
        //标记是否所有链表都结束
        int endCount=0;

        int min=0;
        int index=0;
        int[] values=new int[lists.length];
        OneWayNode<Integer> newNode=new OneWayNode(0);
        OneWayNode<Integer> tmp= newNode;
        boolean isFirst=true;
        for(int i=0;i<lists.length;i++){
            if(endCount<length){
                OneWayNode<Integer> cur=lists[i];
                if(null != cur){
                    if(isFirst){
                        min=cur.t;
                        index=i;
                        isFirst=false;
                    }else if(cur.t<min){
                        min=cur.t;
                        index=i;
                    }
                }else{
                    endCount++;
                    if (endCount==length){
                        continue;
                    }
                }

                if(i==length-1){
                    isFirst=true;
                    OneWayNode<Integer> n1=new OneWayNode<Integer>(min);
                    tmp.next=n1;
                    tmp=n1;
                    lists[index]=lists[index].next;
                    i=-1;
                    endCount=0;
                }
            }
        }
        return newNode.next;
    }
}
