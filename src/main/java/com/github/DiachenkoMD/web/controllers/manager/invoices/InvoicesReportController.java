package com.github.DiachenkoMD.web.controllers.manager.invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.HTierRGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;

import static com.github.DiachenkoMD.web.utils.Utils.sendException;

@UseGuards({AuthGuard.class, HTierRGuard.class})
@WebServlet("/manage/invoices/report")
public class InvoicesReportController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(InvoicesReportController.class);
    private ManagerService managerService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        managerService = ((ManagerService) config.getServletContext().getAttribute("manager_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for obtaining report with extended invoices data as xlsx file.
     * @param req doesn`t accept anything. Final file will contain all available info.
     * @param resp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            Workbook workbook = managerService.generateInvoicesReport();

            resp.setContentType("application/octet-stream");

            String reportName = "report_"+Utils.localDateFormatter.format(LocalDate.now()).replaceAll("-", "_");
            resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.xlsx\"", reportName));

            workbook.write(resp.getOutputStream());

            workbook.close();
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
