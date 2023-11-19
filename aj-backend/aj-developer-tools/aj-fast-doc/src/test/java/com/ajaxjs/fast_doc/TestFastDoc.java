package com.ajaxjs.fast_doc;

import com.ajaxjs.fast_doc.model.ControllerInfo;
import com.ajaxjs.fast_doc.annotation.CustomAnnotationParser;
import com.ajaxjs.fast_doc.annotation.SpringMvcAnnotationParser;
import com.ajaxjs.fast_doc.doclet.JavaDocParser;
import com.ajaxjs.framework.TestHelper;
import org.junit.Test;

import java.util.ArrayList;

public class TestFastDoc {
	@Test
	public void testDoclet() {
		Params params = new Params();
		params.sources = new ArrayList<>();
//		params.sources.add("D:\\code\\oba\\src\\main\\java\\com\\toway\\oba\\model\\ObaDTO.java");
//		params.sources.add("D:\\code\\oba\\src\\main\\java\\com\\toway\\oba\\model\\Task.java");
		params.sources.add("D:\\code\\oba\\src\\main\\java\\com\\toway\\oba\\service\\IDoService.java");
//		params.sources.add("D:\\code\\oba\\src\\main\\java\\com\\toway\\oba\\service\\IKoEvaService.java");

		params.sourcePath = "d:\\code\\oba\\src\\main\\java\\;D:\\code\\websites2\\uav-common\\src\\main\\java;d:\\code\\aj\\aj-framework\\src\\main\\java;d:\\code\\aj\\aj-util\\src\\main\\java";
		params.classPath = Util
				.getClzPath("D:\\sp42\\profile\\dev\\Eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\droneswarm\\WEB-INF\\lib");
		JavaDocParser.init(params);
	}

	@Test
	public void testDoclet2() {
		Params params = new Params();
		params.sources = new ArrayList<>();
		params.sources.add("D:\\code\\ajaxjs\\aj-framework\\aj-framework\\src\\test\\java\\com\\ajaxjs\\fast_doc\\FooController.java");
		params.sourcePath = "d:\\code\\oba\\src\\main\\java\\;D:\\code\\websites2\\uav-common\\src\\main\\java;d:\\code\\aj\\aj-framework\\src\\main\\java;d:\\code\\aj\\aj-util\\src\\main\\java";

		JavaDocParser.init(params);
	}

	@Test
	public void testSpringMvcAnnotationParser() {
		SpringMvcAnnotationParser p = new SpringMvcAnnotationParser(FooController.class);
		ControllerInfo info = p.parse();

		TestHelper.printJson(info);
	}

	@Test
	public void testCustomAnnotationParser() {
		CustomAnnotationParser p = new CustomAnnotationParser(FooController.class);
		ControllerInfo info = p.parse();

		TestHelper.printJson(info);
	}

	@Test
	public void testFastDoc() {
//		FastDoc.loadBeans("D:\\code\\ajaxjs\\aj-framework\\aj-framework\\src\\test\\java\\", FooBean.class, BarBean.class, InnerClass.class);
//		FastDoc.loadControllersDoc("D:\\code\\ajaxjs\\aj-framework\\aj-framework\\src\\test\\java\\", FooController.class);
//
//		FastDoc.saveToDisk("D:\\code\\ajaxjs\\aj-framework\\aj-ui-widget\\fast-doc\\json.js");

		FastDocRun run = new FastDocRun();
		run.sourceDir = "D:\\code\\ajaxjs\\aj-framework\\aj-framework\\src\\test\\java\\"; // 源码目录，到 java 那一层
		run.beanClasses = new Class<?>[] { FooBean.class, BarBean.class, InnerClass.class }; // 有哪些 Java Bean，都列出来
		run.controllerClasses = new Class<?>[] { FooController.class }; // 有哪些 Spring MVC 控制器类，都列出来
		run.jsonDir = "D:\\code\\ajaxjs\\aj-framework\\aj-ui-widget\\fast-doc\\json.js"; // 最终 JSON 保存的地方

		FastDoc.run(run);
	}

	@Test
	public void testFastDocWorkwe() {
		FastDoc.loadBeans("C:\\code\\aj\\aj-framework\\src\\test\\java\\", FooBean.class, BarBean.class, InnerClass.class);
		FastDoc.loadControllersDoc("C:\\code\\aj\\aj-framework\\src\\test\\java\\", FooController.class);

//		System.out.println(FastDoc.getJsonStr());

		FastDoc.saveToDisk("c:\\temp\\json.js");
	}

}
