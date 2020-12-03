package com.github.paganini2008.springworld.blogonline.service;

import org.springframework.stereotype.Service;

import com.github.paganini2008.springworld.blogonline.dao.BloggerDao;
import com.github.paganini2008.springworld.blogonline.entity.Blogger;
import com.github.paganini2008.springworld.support.BaseService;

/**
 * 
 * BloggerService
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
@Service
public class BloggerService extends BaseService<Blogger, Long, BloggerDao> {

	public BloggerService(BloggerDao dao) {
		super(dao);
	}

}
