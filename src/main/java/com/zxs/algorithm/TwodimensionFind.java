package com.zxs.algorithm;

import java.util.Random;

public class TwodimensionFind {
 private int[][] arr=new int[8][7];
 
 public void init() {
	 for(int i=0;i<8;i++) {
		 for(int j=0;j<7;j++) {
			 int data=new Random().nextInt(100)+1;
			 arr[i][j]=data;
		 }
	 }
 }
 
 public void sort() {
	 //横着排序
	 for(int i=0;i<8;i++) {
       int harr[]=arr[i];
       arr_sort(harr);
	 }
	 //竖着排序
	 int temp;
	 
	 //循环所有列
	 for(int i=0;i<7;i++) {  //列
		 //负责每一列的排序
		 for(int j=0;j<7;j++) {  //列
			 for(int n=0;n<8-1;n++) { //行
				 if (arr[n][j]>arr[n+1][j]) {
					temp=arr[n][j];
					arr[n][j]=arr[n+1][j];
					arr[n+1][j]=temp;
				}   
			 }
		 }
	 }
	 
 }
 
 private void arr_sort(int [] arr) {
	 int temp;
	 for(int i=0;i<arr.length-1;i++) {
		 for(int j=i+1;j<arr.length;j++) {
			 if (arr[i]>arr[j]) {
				temp=arr[i];
				arr[i]=arr[j];
				arr[j]=temp;
			 }
		 }
	 }
 }
 
 public int find(int data) {
	 for(int i=0;i<8;i++) {
		 for(int j=0;j<7;j++) {
			 
		 }
	 }
	return data;
 }
 public static void main(String[] args) {
	 TwodimensionFind tf=new TwodimensionFind();
	 tf.init();
	 tf.sort();
	 
	 System.out.println(tf.arr.length);
 }
}
