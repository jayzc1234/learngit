package com.zxs.server.service.gugeng.salemanage;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExportDeliveryOrderPdfService {

	@Autowired
	private ProductionManageOutboundMapper outboundMapper;

	private static final int DEFAULT_MINIMUM_HEIGHT =45;

	@Value("${pdf.font.path}")
	private String fontPath;

	@Autowired
	private CommonUtil commonUtil;

	private static Font font;

	@PostConstruct
	public void init(){
		String system = System.getProperty("os.name").toLowerCase();
		if (system.indexOf("windows")>=0){
			fontPath = "F:\\gitrepository\\simhei.ttf";
		}
		BaseFont baseFontHei = null;
		try {
			baseFontHei = BaseFont.createFont(fontPath,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e) {
			log.error("加载pdf字体失败",e);
		} catch (IOException e) {
			log.error("加载pdf字体失败",e);
		}
		font = new Font(baseFontHei);
	}

	@Autowired
	private ProductManageOrderMapper orderMapper;

	@Autowired
	private ProductionManageOrderProductMapper orderProductMapper;

	public  void exportPdf(Long orderId,OutputStream outputStream) throws Exception {
        log.info("============================执行pdf导出==========================");
		ProductionManageOrder order = orderMapper.selectById(orderId);
		if (null==order){
			CommonUtil.throwSuperCodeExtException(500,"订单不存在");
		}
		com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A3, 0, 0, 1, 1);//上下左右页边距
		try {
			PdfWriter.getInstance(document, outputStream);
			//打开文本
			document.open();
			BaseFont baseFontHei = BaseFont.createFont(fontPath,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			//标题
			Paragraph paragraph = new Paragraph(80);//边距
			//1 2 3  中右左
			paragraph.setAlignment(1);  //对齐方式
			Font font = new Font(baseFontHei);//字体
			font.setSize(14);//字体大小
			paragraph.setFont(font);//设置段落字体
			Chunk chunk = new Chunk("配送验收单");
			chunk.setLineHeight(80);
			paragraph.add(chunk);
			paragraph.setSpacingAfter(20);//往下距离200
			document.add(paragraph);

			Paragraph paragraph1 = getParagraph("生产单位："+commonUtil.getOrganizationName(), baseFontHei,50f);
			document.add(paragraph1);

			ProductionManageOutbound outbound=outboundMapper.selectOneByOrderId(orderId);
			String createUserName="";
			String outboundDateStr="";
			if (null!=outbound){
				createUserName = outbound.getCreateUserName();
				Date outboundDate = outbound.getOutboundDate();
				if (null!=outboundDate){
					outboundDateStr = CommonUtil.formatDateToStringWithFormat(outboundDate, DateTimePatternConstant.YYYYMMDDHHMMSS);
				}
			}
			Paragraph paragraph2 = getParagraph("出库时间："+outboundDateStr+"                " +
					"                                                                                       " +
					"单号："+order.getOrderNo(), baseFontHei,50f);
			document.add(paragraph2);

			String clientAddress = order.getClientAddress();
			addTable(document,8,new float[]{50,100,50,100,120,230,50,50},new String[]{"客户", order.getClientName(),"联系电话", order.getClientContactPhone(),"配送地址",clientAddress,"总件数：   件", "总价"});

			addTable(document,8,new float[]{50,100,50,100,50,300,50,50},new String[]{"序号", "品名","规格", "数量","单价","客户验收情况（是或否)","备注",""});


			List<ProductionManageOrderProduct> orderProducts = orderProductMapper.selectByOrderId(orderId);
			String totalNumStr="";
			if (CollectionUtils.isNotEmpty(orderProducts)){
				Double bigDecimalTotalNum=null;
				Integer intTotalNum=null;
				Byte orderType = order.getOrderType();
				String serbNumb="";
				String num="";
				String unit="";
				for (int i=0;i<orderProducts.size();i++){
					if (i<10){
						serbNumb="0"+(i+1);
					}else {
						serbNumb=String.valueOf(i+1);
					}

					ProductionManageOrderProduct orderProduct = orderProducts.get(i);
					String productSpecName = orderProduct.getProductSpecName();
					OrderTypeEnum byStatus = OrderTypeEnum.getByStatus(orderType);
					switch (byStatus){
						case WEIGHT:
							unit="千克";
							num=orderProduct.getOrderWeight()+unit;
							bigDecimalTotalNum=CommonUtil.doubleAdd(bigDecimalTotalNum,orderProduct.getOrderWeight());
							break;
						case NUM:
							unit="个";
							num=orderProduct.getProductNum()+unit;
							intTotalNum=CommonUtil.integerAdd(intTotalNum,orderProduct.getProductNum());
							break;
						case BOX_NUN:
							unit="箱";
							num=orderProduct.getOrderQuantity()+unit;
							intTotalNum=CommonUtil.integerAdd(intTotalNum,orderProduct.getOrderQuantity());
							break;
						case PORTION:
							unit="份";
							num=orderProduct.getGgPartionNum()+unit;
							intTotalNum=CommonUtil.integerAdd(intTotalNum,orderProduct.getGgPartionNum());
							break;
						default:
							break;
					}
					addTable(document,8,new float[]{50,100,50,100,50,300,50,
							50},new String[]{serbNumb,
							orderProduct.getProductName(),productSpecName,
							num,orderProduct.getUnitPrice(),"","",
							orderProduct.getTotalPrice().toString()});
				}
				//获得最终的统计数量
				if (null==bigDecimalTotalNum){
					totalNumStr=String.valueOf(intTotalNum)+unit;
				}else {
					totalNumStr=String.valueOf(bigDecimalTotalNum)+unit;
				}
			}
			addTable(document,4,new float[]{150,40,510,50},new String[]{"合计", "",totalNumStr, order.getOrderMoney()});

			addTable(document,4,new float[]{150,40,510,50},new String[]{"客户签字", "","", ""});

			Paragraph paragraph3 = getParagraph("出库人："+ (Objects.isNull(createUserName)?"":createUserName) +"  质检员："+(Objects.isNull(createUserName)?"":createUserName)+"      配送员：                              客户签收： ", baseFontHei,50f);
			document.add(paragraph3);
		}catch (Exception e){
			log.info("导出异常：",e);
		}finally{
			document.close();
		}
	}

	private void addTable(com.itextpdf.text.Document document,int columnNum, float[] columnWidth,  String[] cellValues) throws Exception {
		PdfPTable table = new PdfPTable(columnNum);
		table.setTotalWidth(columnWidth);
		table.setLockedWidth(true);//宽度算正确
		for (String cell : cellValues) {
			table.addCell(drawPdfPCell(cell,10,1,DEFAULT_MINIMUM_HEIGHT));
		}
		document.add(table);
	}

	private Paragraph getParagraph(String content, BaseFont baseFontHei,Float indentationLeft) {
		if (null==indentationLeft){
			indentationLeft=0f;
		}
		Paragraph paragraph1 = new Paragraph(10);
		//1 2 3  中右左
		paragraph1.setAlignment(3);  //对齐方式
		Font font1 = new Font(baseFontHei);//字体
		font1.setSize(10);
		paragraph1.setFont(font1);
		Chunk chunk1 = new Chunk( content);
		paragraph1.add(chunk1);
		paragraph1.setSpacingAfter(5);//往下距离200
		paragraph1.setIndentationLeft(indentationLeft);
		return paragraph1;
	}


	private  PdfPCell drawPdfPCell(String cellText, float size, int alignment, int minimumHeight ) throws Exception{
		//为null会报错  防止报错
		if(StringUtils.isBlank(cellText)){
			cellText=" ";
		}
		//表格开始
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(alignment);  //对齐方式
		font.setSize(size);//字体大小
		paragraph.setFont(font);//设置段落字体
		Chunk chunk = new Chunk(cellText);
		paragraph.add(chunk);
		PdfPCell cell = new PdfPCell();
		cell.setUseAscender(true);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);//设置cell垂直居中
		cell.setMinimumHeight(minimumHeight);//设置单元格最小高度，当前行最小高度
		cell.addElement(paragraph);
		return cell;
	}
}
