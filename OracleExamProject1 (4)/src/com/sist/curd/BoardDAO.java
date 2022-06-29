package com.sist.curd;
import java.util.*;// VO(게시물 한개) => List (Collection)
import java.sql.*; // Connection , Statement , ResultSet
/*
 *     JDBC 
 *     ----- 
 *       자바와 오라클 연동 => 라이브러리 
 *       1) 드라이버 설정 (ojdbc8.jar)
 *       2) 오라클 연결 
 *       3) SQL문장 전송 (***********) 
 *       4) 결과값 받기 
 *       5) 오라클 닫기 
 */
public class BoardDAO {
     // 연결 객체 
	 private Connection conn;
	 // SQL문장 전송 => 결과값을 가지고 오는 객체
	 private PreparedStatement ps;
	 // 오라클 서버 주소 설정 
	 private final String URL="jdbc:oracle:thin:@localhost:1521:XE";
	 
	 // 드라이버 등록 ==> 한번만 설정 
	 public BoardDAO()
	 {
		 try
		 {
			 Class.forName("oracle.jdbc.driver.OracleDriver");//리플렉션 (이름등록 => 모든 정보를 처리)
			 // 변수값 주입 , 메소드 호출 , 생성자 제어 가능, 매개변수 확인
			 // DI 
			 // 스프링 => 클래스관리 => 파일 (XML)
		 }catch(Exception ex){}
	 }
	 // 오라클 연결 
	 public void getConnection()
	 {
		 try
		 {
			 conn=DriverManager.getConnection(URL,"hr","happy");
			 // conn hr/happy
		 }catch(Exception ex){}
	 }
	 // 오라클 닫기
	 public void disConnection()
	 {
		 try
		 {
			 if(ps!=null) ps.close();
			 if(conn!=null) conn.close();
			 // exit
		 }catch(Exception ex){}
	 }
	 // 기능 
	 // 1. 로그인 ======> COUNT
	 public String isLogin(String id,String pwd)
	 {
		 // NOID , NOPWD , OK  => 1,2,3 => 상수 
		 // 리팩토링 => 가독성 
		 String result="";
		 try
		 {
			 // 1. 연결
			 getConnection();
			 // 2. SQL문장 전송
			 // 2-1. ID의 존재 여부 확인 
			 String sql="SELECT COUNT(*) "
					   +"FROM boardMember "
					   +"WHERE id=?";
			 // 0(X) , 1(O) => ID중복체크 , 로그인 , 검색 결과 
			 // 3. 결과값 받기 
			 ps=conn.prepareStatement(sql);
			 ps.setString(1, id);
			 ResultSet rs=ps.executeQuery();
			 rs.next(); // 커서 위치 변경 
			 //rs.previous();
			 int count=rs.getInt(1);
			 rs.close();
			 
			 // => 처리 
			 if(count==0)
			 {
				 result="NOID";
			 }
			 else  // id존재 
			 {
				 // 1. 비밀번호 검색 
				 sql="SELECT pwd "
				    +"FROM boardMember "
				    +"WHERE id=?";
				 ps=conn.prepareStatement(sql);
				 ps.setString(1, id);
				 rs=ps.executeQuery();
				 rs.next();
				 String db_pwd=rs.getString(1);
				 rs.close();
				 
				 if(db_pwd.equals(pwd))//로그인
				 {
					 result="OK";
				 }
				 else//비밀번호가 틀리다 
				 {
					 result="NOPWD";
				 }
			 }
			 
		 }catch(Exception ex)
		 {
			 // 에러 확인 
			 ex.printStackTrace();
		 }
		 finally
		 {
			 // 오라클 해제 
			 disConnection();
		 }
		 return result;
		 
	 }
	 // 2. 목록 읽기 ====> 페이징 => 인라인뷰 (서브쿼리)
	 public List<BoardVO> boardListData()
	 {
		 List<BoardVO> list=
				 new ArrayList<BoardVO>();
		 try
		 {
			 // 1. 연결 
			 getConnection();
			 // 2. sql문장 
			 String sql="SELECT no,subject,name,regdate,hit "
					   +"FROM freeboard "
					   +"ORDER BY 1 DESC";
			 // 3. 오라클 전송 
			 ps=conn.prepareStatement(sql);
			 // 4. 결과값 저장 
			 ResultSet rs=ps.executeQuery();
			 // 5. List에 값을 채운다 
			 // DAO => 1.VO , 2.DAO
			 while(rs.next())
			 {
				 BoardVO vo=new BoardVO();
				 vo.setNo(rs.getInt(1));
				 vo.setSubject(rs.getString(2));
				 vo.setName(rs.getString(3));
				 vo.setRegdate(rs.getDate(4));
				 vo.setHit(rs.getInt(5));
				 
				 list.add(vo);
			 }
			 rs.close();
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			 disConnection();
		 }
		 return list;
	 }
	 // 3. 상세보기 ==> WHERE 제목 => String 
	 // 제목 , 이름 => VO 
	 public BoardVO boardDetailData(int no,int type)
	 {
		 BoardVO vo=new BoardVO();
		 try
		 {
			 // 연결 
			 getConnection();
			 // SQL문장 
			 if(type==1) // type=1 => 조회수 증가 , type=2 수정 
			 {
			   String sql="UPDATE freeboard SET "
					   +"hit=hit+1 "
					   +"WHERE no=?";
			 // 조회수 증가 
			   ps=conn.prepareStatement(sql);
			   ps.setInt(1, no);
			   ps.executeUpdate();
			 }
             
			 // 실제 데이터 읽기
			 String sql="SELECT no,name,subject,content,regdate,hit "
			    +"FROM freeboard "
			    +"WHERE no=?";
			 
			 ps=conn.prepareStatement(sql);
			 ps.setInt(1, no);
			 
			 ResultSet rs=ps.executeQuery();
			 rs.next();
			 vo.setNo(rs.getInt(1));
			 vo.setName(rs.getString(2));
			 vo.setSubject(rs.getString(3));
			 vo.setContent(rs.getString(4));
			 vo.setRegdate(rs.getDate(5));
			 vo.setHit(rs.getInt(6));
			 rs.close();
			 
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			 disConnection();
		 }
		 return vo;
	 }
	 // 4. 글쓰기 ==> INSERT
	 public void boardInsert(BoardVO vo)
	 {
		 try
		 {
			 // 연결
			 getConnection();
			 // SQL문장 
			 String sql="INSERT INTO freeboard(no,name,subject,content,pwd) VALUES("
					   +"(SELECT NVL(MAX(no)+1,1) FROM freeboard),?,?,?,?)";
			 // 오라클로 전송 => 네트워크 
			 ps=conn.prepareStatement(sql);
			 // ?에 값을 채운다 
			 ps.setString(1, vo.getName());
			 ps.setString(2, vo.getSubject());
			 ps.setString(3, vo.getContent());
			 ps.setString(4, vo.getPwd());
			 
			 // 실행 명령 
			 ps.executeUpdate(); //commit => autoCommit()
			 // COMMIT (데이터가 변경) => INSERT , UPDATE , DELETE
			 // 데이터를 읽는 기능이 없다 
			 //conn.setAutoCommit(false);
			 //conn.commit();
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			 disConnection();
		 }
	 }
	 // 5. 수정  ===> UPdate
	 public boolean boardUpdate(BoardVO vo)
	 {
		 boolean bCheck=false;
		 try
		 {
			 getConnection();
			 // SQL문장 
			 String sql="SELECT pwd "
					   +"FROM freeboard "
					   +"WHERE no=?";
			 ps=conn.prepareStatement(sql);
			 ps.setInt(1, vo.getNo());
			 ResultSet rs=ps.executeQuery();
			 rs.next();
			 String db_pwd=rs.getString(1);
			 rs.close();
			 
			 if(db_pwd.equals(vo.getPwd()))
			 {
				 bCheck=true;
				 // 실제 수정 
				 sql="UPDATE freeboard SET "
				    +"name=?,subject=?,content=? "
					+"WHERE no=?";
				 ps=conn.prepareStatement(sql);
				 ps.setString(1, vo.getName());
				 ps.setString(2, vo.getSubject());
				 ps.setString(3, vo.getContent());
				 ps.setInt(4, vo.getNo());
				 
				 ps.executeUpdate();
				 
			 }
			 else
			 {
				 bCheck=false;
			 }
			 
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			disConnection(); 
		 }
		 return bCheck;
	 }
	 // 6. 삭제   ===> Delete
	 public void boardDlete(int no)
	 {
		 try
		 {
			 getConnection();
			 String sql="DELETE FROM freeboard WHERE no=?";
			 ps=conn.prepareStatement(sql);
			 ps.setInt(1, no);
			 ps.executeUpdate();
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			 disConnection();
		 }
	 }
	 // 7. 찾기   ===> Like 
	 public List<BoardVO> boardFindData(String fd,String ss)
	 {
		 List<BoardVO> list=
				 new ArrayList<BoardVO>();
		 try
		 {
			 getConnection();
			 String sql="SELECT name,subject "
					 +"FROM freeboard "
					 +"WHERE "+fd+" LIKE '%'||?||'%'";
			 //                     LIKE CONCAT('%',?,'%')
			 //                     LIKE '%"+ss+"%'"
			 ps=conn.prepareStatement(sql);
			 ps.setString(1, ss);
			 ResultSet rs=ps.executeQuery();
			 while(rs.next())
			 {
				 BoardVO vo=new BoardVO();
				 vo.setName(rs.getString(1));
				 vo.setSubject(rs.getString(2));
				 list.add(vo);
			 }
			 rs.close();
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 finally
		 {
			 disConnection();
		 }
		 return list;
	 }
	 // JOIN 
}














