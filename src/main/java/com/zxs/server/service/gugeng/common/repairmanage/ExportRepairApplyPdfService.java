package com.zxs.server.service.gugeng.common.repairmanage;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageConsumeStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageRepairManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageConsumeStuffMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageRepairManageMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;

@Service
@Slf4j
public class ExportRepairApplyPdfService extends CommonUtil {
	@Autowired
	private ProductionManageRepairManageMapper repairManageMapper;

	@Autowired
	private ProductionManageConsumeStuffMapper consumeStuffMapper;

	private static final int DEFAULT_MINIMUM_HEIGHT =45;
	public  void exportPdf(Long id, OutputStream outputStream) throws Exception {
		ProductionManageRepairManage repairManage = repairManageMapper.selectById(id);
		if (null==repairManage){
			CommonUtil.throwSuperCodeExtException(500,"维修记录不存在");
		}
		Document document = new Document(PageSize.A4, 70, 70, 20, 100);//上下左右页边距
		try {
			PdfWriter.getInstance(document, outputStream);
			//打开文本
			document.open();
			String fontPath=getClass().getResource("/")+"simhei.ttf";
			log.info("导出字体路径："+fontPath);
			BaseFont baseFontHei = BaseFont.createFont(fontPath,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			//标题
			Paragraph paragraph = new Paragraph(80);//边距
			//1 2 3  中右左
			paragraph.setAlignment(1);  //对齐方式
			Font font = new Font(baseFontHei);//字体
			font.setSize(14);//字体大小
			paragraph.setFont(font);//设置段落字体
			Chunk chunk = new Chunk("工程维修单");
			chunk.setLineHeight(80);
			paragraph.add(chunk);
			paragraph.setSpacingAfter(20);//往下距离200
			document.add(paragraph);

			Paragraph paragraph1 = new Paragraph(10);
			//1 2 3  中右左
			paragraph1.setAlignment(2);  //对齐方式
			Font font1 = new Font(baseFontHei);//字体
			font1.setSize(10);
			paragraph1.setFont(font1);
			Chunk chunk1 = new Chunk("编号："+ repairManage.getRepairNo());
			paragraph1.add(chunk1);
			paragraph1.setSpacingAfter(5);//往下距离200
			document.add(paragraph1);

			PdfPTable table = new PdfPTable(4);
			table.setTotalWidth(500);
			float[] columnWidth1={100,150,100,150};
			table.setTotalWidth(columnWidth1);
			table.setLockedWidth(true);//宽度算正确
			setTable(table, columnWidth1,new String[]{"报修部门：", repairManage.getCallRepairDepartmentName(),"保修人员：", repairManage.getCallRepairEmployeeName()}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);


			//画表头第二行
			table = new PdfPTable(4);
			table.setTotalWidth(500);
			table.setTotalWidth(columnWidth1);
			table.setLockedWidth(true);//宽度算正确
			setTable(table, columnWidth1,new String[]{"报修时间：", CommonUtil.formatDateToStringWithFormat(repairManage.getCallRepairDate(), DateTimePatternConstant.YYYY_MM_DD),"接单时间：", CommonUtil.formatDateToStringWithFormat(repairManage.getOrderTakerDate(), DateTimePatternConstant.YYYY_MM_DD)}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第三行
			table = new PdfPTable(4);
			table.setTotalWidth(500);
			table.setTotalWidth(columnWidth1);
			table.setLockedWidth(true);//宽度算正确
			setTable(table, columnWidth1,new String[]{"维修部门:", repairManage.getRepairDepartmentName(),"接单人员：", repairManage.getOrderTakerName()}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第4行
			table = new PdfPTable(3);
			table.setTotalWidth(500);
			float[] columnWidth4={300,100,100};
			setTable(table, columnWidth4,new String[]{"维修内容","申请完成时间","工程预计完成时间"}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第5行
			table = new PdfPTable(3);
			table.setTotalWidth(500);
			float[] columnWidth5={300,100,100};
			setTable(table, columnWidth4,new String[]{repairManage.getRepairContent(), CommonUtil.formatDateToStringWithFormat(repairManage.getApplyDoneDate(), DateTimePatternConstant.YYYY_MM_DD), CommonUtil.formatDateToStringWithFormat(repairManage.getEstimateDoneRepairDate(), DateTimePatternConstant.YYYY_MM_DD)}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第6行
			table = new PdfPTable(1);
			table.setTotalWidth(500);
			float[] columnWidth6={500};
			setTable(table, columnWidth6,new String[]{"消耗材料名称规格数量"},18);
			document.add(table);
			List<ProductionManageConsumeStuff>consumeStuffList=consumeStuffMapper.selectByApplyId(repairManage.getId());
			if (CollectionUtils.isNotEmpty(consumeStuffList)){
				//画表头第5行
				table = new PdfPTable(3);
				table.setTotalWidth(500);
				float[] columnWidth={168,166,166};
				setTable(table, columnWidth,new String[]{"材料名称","规格","数量"},26);
				document.add(table);
				for (ProductionManageConsumeStuff productionManageConsumeStuffDTO : consumeStuffList) {
					String stuffName = productionManageConsumeStuffDTO.getStuffName();
					String specification = productionManageConsumeStuffDTO.getSpecification();
					Integer stuffAmount = productionManageConsumeStuffDTO.getStuffAmount();
					table = new PdfPTable(3);
					table.setTotalWidth(500);
					float[] s_columnWidth={168,166,166};
					setTable(table, s_columnWidth,new String[]{stuffName,specification,stuffAmount+""},30);
					document.add(table);
				}
			}

			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			float[] columnWidth7={190,310};
			setTable(table, columnWidth7,new String[]{"报修部门确认：",""}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			setTable(table, columnWidth7,new String[]{"维修部门意见：",""}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			setTable(table, columnWidth7,new String[]{"报修部门主管领导审批：",""}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			setTable(table, columnWidth7,new String[]{"维修部门主管领导审批：",""}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);

			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			float[] columnWidth8={250,250};
			setTable(table, columnWidth8,new String[]{"维修人员签字、完成日期、时间","报修部门跟进人员确认签字"}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);


			//画表头第7行
			table = new PdfPTable(2);
			table.setTotalWidth(500);
			float[] columnWidth9={250,250};
			setTable(table, columnWidth8,new String[]{"",""}, DEFAULT_MINIMUM_HEIGHT);
			document.add(table);
		}finally {
			document.close();
		}

	}

	//
	private  void setTable(PdfPTable table, float[] columnWidth2, String[] cells, int minimumHeight) throws Exception {
		table.setTotalWidth(500);
		table.setTotalWidth(columnWidth2);
		table.setLockedWidth(true);//宽度算正确
		for (String cell : cells) {
			table.addCell(drawPdfPCell(cell,10,1,minimumHeight));
		}
	}

	private  PdfPCell drawPdfPCell(String cellText, float size, int alignment, int minimumHeight ) throws Exception{
		//为null会报错  防止报错
		if(cellText==null){
			cellText=" ";
		}
		String fontPath=getClass().getResource("/")+"simhei.ttf";
		BaseFont baseFontHei = BaseFont.createFont(fontPath,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		//表格开始
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(alignment);  //对齐方式
//		Font font=new Font();
		Font font = new Font(baseFontHei);//字体
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
