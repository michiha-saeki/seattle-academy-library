package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;

/**
 * Handles requests for the application home page.
 */
@Controller //APIの入り口
public class BulkBookController {
    final static Logger logger = LoggerFactory.getLogger(BulkBookController.class);

    @Autowired
    private BooksService booksService;

    @RequestMapping(value = "/bulkBook", method = RequestMethod.GET) //value＝actionで指定したパラメータ
    //RequestParamでname属性を取得
    public String login(Model model) {
        return "bulkBook";
    }

    /**
     * 書籍情報を登録する
     * @param file CSVファイル
     */
    @Transactional
    @RequestMapping(value = "/bulkinsertBook", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    public String insertBook(Locale locale,
            @RequestParam("bulk_form") MultipartFile file,
            Model model) {
        logger.info("Welcome insertBooks.java! The client locale is {}.", locale);

        //確認する作業
        try {

            List<BookDetailsInfo> bookcsv = new ArrayList<BookDetailsInfo>();
            String line = null; //
            InputStream stream = file.getInputStream();
            Reader reader = new InputStreamReader(stream);
            BufferedReader buf = new BufferedReader(reader);
            int rowCount = 1; //csvの行の番号（何行目にいるのか）
            boolean flag = false; //変数にflagに初期値としてfalseを入れる
            String errorMessage = ""; //後々足していくのでこの時点では初期値は空文字を置いておく

            //一行ずつ読み出す
            //List<BookDeatilsInfo>型のbookListに格納
            //while文の中では読んで値を保持していく作業が行われている
            while((line = buf.readLine()) != null) {
                String[] bookdata = line.split(",");  //行をカンマ区切りで配列に変換

                //パラメーター（変数）で受け取った書籍情報を書籍詳細情報格納Dtoに格納する
                BookDetailsInfo bookInfo = new BookDetailsInfo();

                //必須項目のbookData[0]-[3]が空でないか、isEmptyは文字の長さが０の状態
                if (bookdata[0].isEmpty() || bookdata[1].isEmpty() ||  bookdata[2].isEmpty() || bookdata[3].isEmpty()); {
                   errorMessage += rowCount + "行目で必要な情報がありません。";  //ここでは、格納してるだけ
                    flag = true; //trueの時にif文は実行されるので、エラーが起きたことを示ためにtrueを代入しておくこと
                }
                
                //出版日とISBNのバリデーションチェック
                //もし空だった場合は上のエラーで引っかかる
                //nullじゃなかった時のif文＝入力されている場合
                if (bookdata[3] != null) {
                    
                  //出版日のバリデーションチェック
                    try {
                        //日付の確認
                        SimpleDateFormat pd = new SimpleDateFormat("yyyyMMdd");
                        pd.setLenient(false);
                        pd.parse(bookdata[3]);

                    } catch (ParseException pe) {
                        errorMessage += rowCount + "行目の出版日はYYYYMMDDの形式で入力してください"; 
                        flag = true; //trueの時にif文は実行されるので、エラーが起きたことを示ためにtrueを代入しておくこと
                    }
                    
                }

              //ISBNの確認
                if (bookdata[4] != null && !(bookdata[4].isEmpty())
                       && !(bookdata[4].matches("([0-9]{10}|[0-9]{13})"))) {
                    errorMessage += rowCount + "行目のISBNは半角数字10桁か13桁で入力してください"; 
                    flag = true;
                }
                
              
                // パラメータで受け取った書籍情報をDtoに格納する。
                //bookData[0]=タイトル、bookData[1]=著者名、bookData[2]=出版社、bookData[3]=出版社、
                //bookData[4]=ISBN、bookData[5]=説明
                bookInfo.setTitle(bookdata[0]);
                bookInfo.setAuthor(bookdata[1]);
                bookInfo.setDescription(bookdata[2]);
                bookInfo.setPublisher(bookdata[3]);
                bookInfo.setPublishDate(bookdata[4]);
                bookInfo.setIsbn(bookdata[5]);
                
                bookcsv.add(bookInfo);
                rowCount++;

            }

            //エラーがあった場合の処理
            //ここにこのif文をおくことで、どこでどんなエラーが出たのか全部わかる
            //while文の中に入れてしまうと、例えば1つ目のエラーが出た時にそれだけでreturnしてしまって
            //二行目にもエラーがあるのにそれをいちいち直さなくてはならなくなる
            if (flag) {
                model.addAttribute("errorMessage", errorMessage);
                return "bulkBook";
            }

            //書籍情報を新規登録する
            for (BookDetailsInfo book : bookcsv) {
                booksService.registBook(book);
                return "bulkBook";
            }


            model.addAttribute("resultMessage", "登録完了");

            // TODO 登録した書籍の詳細情報を表示するように実装

            model.addAttribute("bookDetailsInfo", booksService.getBookInfo(booksService.getBookId()));

            //  詳細画面に遷移
            return "bulkBook";


        } catch (IOException i) { //ファイルがないときに見つからない
            model.addAttribute("errorMessage", "CSVファイルの読み込みエラーが発生しました。");
            return "bulkBook";
        } catch (Exception e) { //その他の例外
            model.addAttribute("errorMessage", "CSVファイルの読み込みエラーが発生しました。");
            return "bulkBook";
        }
    }
}

