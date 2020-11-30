package zxs.test.model;

import lombok.Data;

/**
 *单向链表
 */
@Data
public class OneWayNode<T> {

    public T t;

    public OneWayNode<T> next;

    public OneWayNode(T t){
        this.t=t;
    }
    /**
     * 根据数组生成单链表
     * @param ts
     * @param <T>
     * @return
     */
    public static <T>OneWayNode addNodes(T[] ts){
        OneWayNode<T> oneWayNode=new OneWayNode<T>(null);
        OneWayNode<T> tmp=oneWayNode;
        for (T t1 : ts) {
            OneWayNode<T> newNode=new OneWayNode<T>(t1);
            tmp.next=newNode;
            tmp=newNode;
        }
        return oneWayNode.next;
    }

    /**
     * 根据数组生成单链表
     * @param ts
     * @param <T>
     * @return
     */
    public static <T>OneWayNode addNode(T ts,OneWayNode oneWayNode){
        if (null == oneWayNode){
            oneWayNode=new  OneWayNode<T>(ts);
        }else {
            OneWayNode<T> tmp=oneWayNode;
            while (null != tmp.next){
                tmp=tmp.next;
            }
            tmp.next=new OneWayNode<T>(ts);
        }
        return oneWayNode;
    }


    public static void main(String[] args) {
        Integer[] values=new Integer[]{1,2,3,4,5};
        OneWayNode oneWayNode = OneWayNode.addNodes(values);
        reverse(oneWayNode);
    }

    /**
     * 高效反转单链表
     * @param oneWayNode
     */
    private static void reverse(OneWayNode oneWayNode)  {

        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
