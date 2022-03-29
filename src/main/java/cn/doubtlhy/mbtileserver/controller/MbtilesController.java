package cn.doubtlhy.mbtileserver.controller;

import cn.doubtlhy.mbtileserver.compoent.MbtilesListBean;
import cn.doubtlhy.mbtileserver.model.MbtilesInfo;
import cn.doubtlhy.mbtileserver.model.UpdateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/services")
public class MbtilesController {

    private static final Logger log = LoggerFactory.getLogger(MbtilesController.class);
    @Autowired
    MbtilesListBean mbtilesListBean;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, path = {"", "/"}, produces = {"application/json;charset=UTF-8"})
    public List<MbtilesInfo> serviceList(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        if (requestURL.endsWith("/")) {
            requestURL = requestURL.substring(0, requestURL.length() - 1);
        }
        mbtilesListBean.setRootURL(String.format("%s", requestURL));
        return mbtilesListBean.getAllmbfiles();
    }

    @ResponseBody
    @RequestMapping(path = "refresh", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    public UpdateStatus refreshFolder() {
        UpdateStatus status = new UpdateStatus();
        try {
            mbtilesListBean.setmbtilesValue();
            status.code = 0;
            status.msg = "refresh succeeded";
        } catch (Exception e) {
            status.code = -1;
            status.msg = e.getMessage();
        }
        return status;
    }
}
