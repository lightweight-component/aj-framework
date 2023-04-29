package com.ajaxjs.fast_doc;

import com.ajaxjs.fast_doc.InnerClass.HelloBean;
import com.ajaxjs.fast_doc.InnerClass.HelloWorld;
import com.ajaxjs.framework.spring.easy_controller.anno.ControllerMethod;
import com.ajaxjs.framework.spring.easy_controller.anno.Example;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 抵近侦察区域控制器
 * 
 * @author Frank Cheung sp42@qq.com
 *
 */
@RestController
@RequestMapping("/close")
public interface FooController {
	/**
	 * 保存所有侦查点
	 * 
	 * @param taskId
	 * @param detectPoints
	 * @return
	 */
	@PostMapping("/detect_point/{taskId}")
	Boolean saveAllDetectPoint(@PathVariable @Example("{\"a\":20220232}") String taskId, @RequestBody List<Map<String, Object>> detectPoints);

	/**
	 * 
	 * @param bean 提交的实体
	 * @return
	 */
	@PutMapping("/bean")
	@ControllerMethod("保存所有侦查点2")
	BarBean saveDetectPoint(@RequestBody FooBean bean);

	/**
	 * 航路规划 ABC
	 * 
	 * @param taskId 任务 id
	 * @return
	 */
	@GetMapping("/route_plan/{taskId}")
	@ControllerMethod("航路规划")
	@Example("[\"task-20220232\", \"task-20220233\"]")
	List<String> routePlan(@PathVariable String taskId);

	/**
	 * 资源规划
	 * 
	 * @param taskId 任务 id
	 * @return
	 */
	@GetMapping("/route_plan/{taskId}/foo")
	@ControllerMethod("资源规划")
	@Example("[\"task-20220232\", \"task-20220233\"]")
	List<FooBean> resPlan(@PathVariable String taskId);

	/**
	 * 删除
	 * 
	 * @param taskId
	 * @return
	 */
	@DeleteMapping("/route_plan/{taskId}/foo")
	@Example("[\"task-20220232\", \"task-20220233\"]")
	HelloWorld delete(@RequestBody HelloBean helloBean);
}
