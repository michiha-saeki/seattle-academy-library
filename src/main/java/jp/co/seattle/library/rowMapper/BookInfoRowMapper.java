package jp.co.seattle.library.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import jp.co.seattle.library.dto.BookInfo;

@Configuration
public class BookInfoRowMapper implements RowMapper<BookInfo> {

    @Override
    public BookInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Query結果（ResultSet rs）を、オブジェクトに格納する実装
        BookInfo bookInfo = new BookInfo();

        // bookInfoの項目と、取得した結果(rs)のカラムをマッピングする
        // 出版社、出版日、著者を追記
        bookInfo.setBookId(rs.getInt("id"));
        bookInfo.setTitle(rs.getString("title"));
        bookInfo.setThumbnail(rs.getString("thumbnail_url"));
        bookInfo.setPublisher(rs.getString("publisher"));
        bookInfo.setPublishDate(rs.getString("publish_date"));
        bookInfo.setAuthor(rs.getString("author"));
        return bookInfo;
    }

}