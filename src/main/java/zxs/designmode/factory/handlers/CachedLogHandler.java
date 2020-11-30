package zxs.designmode.factory.handlers;

public class CachedLogHandler implements LogHandler{
	@Override
	public void saveLog(String log) {
       System.out.println("日志"+log+"存入缓存");		
	}
}
