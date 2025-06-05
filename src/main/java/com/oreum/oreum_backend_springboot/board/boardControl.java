package com.oreum.oreum_backend_springboot.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins="http://localhost:3000", allowCredentials="true")
public class boardControl {
	@Autowired boardDAO bd;
	
	@PostMapping("/")
	public List<boardDTO> list(@PathVariable("id") int id) {
		System.out.println("O");
		return bd.getlist();
	}
	
}
