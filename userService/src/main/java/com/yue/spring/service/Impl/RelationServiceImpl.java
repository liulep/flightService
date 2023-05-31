package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.RelationMapper;
import com.yue.spring.pojo.Relation;
import com.yue.spring.service.RelationService;
import org.springframework.stereotype.Service;

@Service
public class RelationServiceImpl extends ServiceImpl<RelationMapper,Relation> implements RelationService {

}
