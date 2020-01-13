/*
 * The MIT License (MIT) Copyright (c) 2014-2017 abel533@gmail.com Permission is hereby granted, free of charge, to any
 * person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.pagehelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Properties;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * QueryInterceptor 规范 详细说明见文档：https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
 * @author liuzh/abel533/isea533
 * @version 1.0.0
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
	ResultHandler.class }), @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class, CacheKey.class, BoundSql.class }), })
public class QueryInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		Object parameter = args[1];
		RowBounds rowBounds = (RowBounds) args[2];
		ResultHandler resultHandler = (ResultHandler) args[3];
		Executor executor = (Executor) invocation.getTarget();
		CacheKey cacheKey;
		BoundSql boundSql;
		//由于逻辑关系，只会进入一次
		if (args.length == 4) {
			//4 个参数时
			boundSql = ms.getBoundSql(parameter);
			cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
		} else {
			//6 个参数时
			cacheKey = (CacheKey) args[4];
			boundSql = (BoundSql) args[5];
		}
		//TODO 自己要进行的各种处理
		//注：下面的方法可以根据自己的逻辑调用多次，在分页插件中，count 和 page 各调用了一次
		return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
	}

	private Interceptor interceptor = this;

	private Class<?>[] interfaces = new Class<?>[] { Executor.class };

	@Override
	public Object plugin(Object target) {
		//只拦截Executor对象，减少目标被代理的次数
		if (target instanceof Executor) {
			return Proxy.newProxyInstance(target.getClass().getClassLoader(), interfaces, (InvocationHandler) (proxy, method, args) -> {
				if (method.getName().equals("query")) {
					return interceptor.intercept(new Invocation(target, method, args));
				}
				return method.invoke(target, args);
			});
		} else {
			return target;
		}
	}

	//	@Override
	//	public Object plugin(Object target) {
	//		return Plugin.wrap(target, this);
	//	}

	@Override
	public void setProperties(Properties properties) {
	}

}
