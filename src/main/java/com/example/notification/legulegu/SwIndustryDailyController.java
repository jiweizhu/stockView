package com.example.notification.legulegu;

import com.example.notification.legulegu.dto.IndustryMetaDto;
import com.example.notification.legulegu.dto.IndustrySeriesDto;
import com.example.notification.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sw")
public class SwIndustryDailyController {

    @Autowired
    private SwIndustryService service;

    @Autowired
    private LeguSyncService leguSyncService;

    @Value("${legu.industry.file.path}")
    private String path;

    @GetMapping("/admin/import-sw-industry")
    public String importIndustry() throws Exception {
        if(Utils.isWinSystem()){
            path = "C:\\kiwi\\notification\\src\\main\\resources\\stockFolder\\shenwan_index_l3.txt";
        }
        int count = service.importFromTxt(path);
        return "成功导入行业数量: " + count;
    }

    @GetMapping("/admin/legu/sync-all")
    public String syncAll(@RequestParam(defaultValue = "false") boolean clean) {
        leguSyncService.syncAllIndustries(clean);
        return "触发全量同步，clean=" + clean;
    }

    /**
     * 只同步某一个行业
     * 示例：GET /admin/legu/sync-one?code=851941.SI&clean=false
     */
    @GetMapping("/admin/legu/sync-one")
    public String syncOne(@RequestParam("code") String code,
                          @RequestParam(defaultValue = "false") boolean clean) {
        leguSyncService.syncSingleIndustry(code, clean);
        return "已同步行业: " + code;
    }

    @GetMapping("/admin/legu/debug")
    public String debug(@RequestParam String code) {
        leguSyncService.debugSingle(code);
        return "OK";
    }


    /**
     * 示例：
     * GET /api/sw/industry-daily?codes=851941.SI,851942.SI&startDate=2021-01-01&endDate=2025-12-31
     */
    @GetMapping("/industry-daily")
    public List<IndustrySeriesDto> queryIndustryDaily(
            @RequestParam("codes") String codes,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<String> codeList = service.parseCodes(codes);
        return service.getSeries(codeList, startDate, endDate);
    }
@Autowired
private SWIndustryRepository swIndustryRepository;

    @GetMapping("/industries")
    public List<IndustryMetaDto> listAllIndustries() {
        return swIndustryRepository.findAll()
                .stream()
                .map(e -> {
                    IndustryMetaDto dto = new IndustryMetaDto();
                    dto.setIndustryCode(e.getIndustryCode());
                    dto.setIndustryName(e.getIndustryName());
                    dto.setStockCount(e.getStockCount());
                    dto.setParentName(e.getParentName());
                    return dto;
                })
                .toList();
    }

}
