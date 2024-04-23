package bi.accounting.service

import java.util.HashMap

interface ReportService {
    fun processReport(reportData: HashMap<*,*>)
}