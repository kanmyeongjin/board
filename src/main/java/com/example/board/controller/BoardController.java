package com.example.board.controller;


import com.example.board.dto.Board;
import com.example.board.dto.LoginInfo;
import com.example.board.service.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// HTTP요청을 받아서 응답하는 컴포넌트, 스프링 부트가 자동으로 Bean으로 생성한다
@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    // 게시물 목록을 보여준다.
    //컨트롤러의 메소드가 리턴하는 문자열은 템플릿 이름이다.
    //http://localhost:8080/ ==> "List"라는 이름의 템플릿을 사용(forward)하여 화면에 출력
    // list를 리턴한다는 것은
    // classpath:/templates/list.html
    @GetMapping("/")
    public String list(@RequestParam(name ="page", defaultValue = "1") int page, HttpSession session, Model model){ //HttpSssion, Model은 Spring이 자동으로 넣어준다.
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        model.addAttribute("loginInfo", loginInfo);

       // int page = 1;
        int totalCount = boardService.getTotalCount();
        List<Board> list = boardService.getBoards(page); //page가 1,2,3,4 ....
        int pageCount = totalCount / 10; // 1
        if(totalCount % 10 > 0){
            pageCount++;
        }
        int currentPage = page;
//        System.out.println("totalCount : "+totalCount);
//        for(Board board : list){
//            System.out.println(board);
//        }
        model.addAttribute("list", list);
        model.addAttribute("pageCount",pageCount);
        model.addAttribute("currentPage", currentPage);
        return "list";
    }

    // /board?id=3 // 파라미터 id , 파라미터 id의 값은 3
    @GetMapping("/board")
    public String board(@RequestParam("boardId") int boardId, Model model){
        System.out.println("boardId : " + boardId );
        //id에 해당하는 게시물 정보 불러오기
        //id에 해당하는 기시물의 조회수 1 증가
        Board board =  boardService.getBoard(boardId);
        model.addAttribute("board", board);
        return "board";
    }

    @GetMapping("/writeForm")
    public String writeForm(HttpSession session, Model model){
        //로그인한 사용자만 글을 써야한다. 로그인을 하지 않았다면 리스트보기로 자동 이동 시킨다.
        // 세션에서 로그인한 정보를 읽어들인다.
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        if(loginInfo == null){ // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }

        model.addAttribute("loginInfo", loginInfo);

        return "writeForm";
    }

    @PostMapping("/write")
    public String write(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session
    ){
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        if(loginInfo == null){ // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }
        System.out.println("title : "+title);
        System.out.println("content : "+content);

        boardService.addBoard(loginInfo.getUserId(), title,  content);
        return "redirect:/";
    }
    //삭제한다. 관리자는 모든 글을 삭제할 수 있다.
    //수정한다.
    @GetMapping("/delete")
    public String delete(
            @RequestParam("boardId") int boardId,
            HttpSession session
    ) {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }

        // loginInfo.getUserId() 사용자가 쓴 글일 경우에만 삭제한다.
        List<String> roles = loginInfo.getRoles(); //리스트가 가지고 있는것중에 찾는거면 for문 없이 contains로 찾기 가능
        if(roles.contains("ROLE_ADMIN")){
            boardService.deleteBoard(boardId);
        }else {
            boardService.deleteBoard(loginInfo.getUserId(), boardId);
        }

        return "redirect:/"; // 리스트 보기로 리다이렉트한다.
    }

    @GetMapping("/updateform")
    public String updateform(@RequestParam("boardId") int boardId, Model model,  HttpSession session){
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }
        // boardId에 해당하는 정보를 읽어와서 updateform 템플릿에게 전달한다.
        Board board = boardService.getBoard(boardId, false);
        model.addAttribute("board", board);
        model.addAttribute("loginInfo", loginInfo);
        return "updateform";
    }

    @PostMapping("/update")
    public String update(@RequestParam("boardId") int boardId,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         HttpSession session
    ){

        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }

        Board board = boardService.getBoard(boardId, false);
        if(board.getUserId() != loginInfo.getUserId()){
            return "redirect:/board?boardId=" + boardId; // 글보기로 이동한다.
        }
        // boardId에 해당하는 글의 제목과 내용을 수정한다.
        boardService.updateBoard(boardId, title, content);
        return "redirect:/board?boardId=" + boardId; // 수정된 글 보기로 리다이렉트한다.
    }
}
