package com.github.pagehelper.test.basic;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.UserMapper;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

public class PluginWrapperTest {

	@Test
	public void test() {
		SqlSession sqlSession = MybatisHelper.getSqlSession();
		final UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
		for (int i = 0; i < 2000; i++) {
			PageHelper.startPage(1, 100).doSelectPage(() -> userMapper.selectAll());
		}
		for (int time = 0; time < 10; time++) {
			long start = System.nanoTime();
			for (int i = 0; i < 200000; i++) {
				PageHelper.startPage(1, 100).doSelectPage(() -> userMapper.selectAll());
			}
			long duration = (System.nanoTime() - start) / 1000000;
			System.out.println(duration);
		}
	}

}
