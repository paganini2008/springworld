package com.github.paganini2008.springworld.blogonline.service;

import org.springframework.stereotype.Service;

import com.github.paganini2008.springworld.blogonline.dao.ArticleTypeDao;
import com.github.paganini2008.springworld.blogonline.entity.ArticleType;
import com.github.paganini2008.springworld.support.BaseService;

/**
 * 
 * ArticleTypeService
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
@Service
public class ArticleTypeService extends BaseService<ArticleType, Long, ArticleTypeDao> {

	public ArticleTypeService(ArticleTypeDao dao) {
		super(dao);
	}

}
