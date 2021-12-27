package ru.learnup.javaqa.utils;

import ru.learnup.javaqa.db.dao.CategoriesMapper;
import ru.learnup.javaqa.db.dao.ProductsMapper;
import java.io.InputStream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

@UtilityClass
public class DbUtils {
    static String resource = "mybatisConfig.xml";
    @SneakyThrows
    public SqlSession getSession(){
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        System.out.println("Подключение установлено!\n");

        return sqlSession;
    }

    public ProductsMapper getProductsMapper(){
        return getSession().getMapper(ProductsMapper.class);
    }

    public CategoriesMapper getCategoryMapper(){
        return getSession().getMapper(CategoriesMapper.class);
    }
}