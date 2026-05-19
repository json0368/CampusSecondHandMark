package com.cshm.campussecondhandmark.common.config;

import com.cshm.campussecondhandmark.common.interceptor.JwtTokenAdminInterceptor;
import com.cshm.campussecondhandmark.common.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Value("${cshm.upload.path:${user.dir}/upload/}")
    private String uploadBasePath;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("注册自定义拦截器");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/user/**", "/api/products", "/api/products/**", "/api/reviews", "/api/reviews/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs");

        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin-api/**")
                .excludePathPatterns(
                        "/admin-api/auth/login",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs");
    }

    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("校园二手交易与即时沟通平台 API 文档")
                .version("1.0")
                .description("用于演示用户、商品、分类与后台审核接口")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.cshm.campussecondhandmark.module"))
                .paths(PathSelectors.any())
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        String path = this.uploadBasePath.endsWith("/") || this.uploadBasePath.endsWith("\\")
                ? this.uploadBasePath : this.uploadBasePath + "/";

        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + path);
    }
}
