package com.example.notification.legulegu;


import com.example.notification.legulegu.dto.IndustryDailyPointDto;
import com.example.notification.legulegu.dto.IndustrySeriesDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SwIndustryService {

    @Autowired
    private SwIndustryDailyRepository industryDailyRepository;


    @Autowired
    private SWIndustryRepository swIndustryRepository;

    public int importFromTxt(String absolutePath) throws Exception {

        File file = new File(absolutePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + absolutePath);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
        );

        String line;
        int count = 0;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            // 示例：850111.SI,种子(8)[种植业]
            String[] parts = line.split(",");
            String code = parts[0].trim();
            String right = parts[1].trim();  // 种子(8)[种植业]

            String name = right.substring(0, right.indexOf("("));
            int stockCount = Integer.parseInt(right.substring(right.indexOf("(") + 1, right.indexOf(")")));
            String parent = right.substring(right.indexOf("[") + 1, right.indexOf("]"));

            SWIndustry industry = new SWIndustry();
            industry.setIndustryCode(code);
            industry.setIndustryName(name);
            industry.setStockCount(stockCount);
            industry.setParentName(parent);

            swIndustryRepository.save(industry);
            count++;
        }

        reader.close();
        return count;
    }


    /**
     * 按多个行业代码 + 日期范围获取 4 个指标曲线
     */
    public List<IndustrySeriesDto> getSeries(
            List<String> industryCodes,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (industryCodes == null || industryCodes.isEmpty()) {
            return Collections.emptyList();
        }

        // 默认查询全部区间
        if (startDate == null) {
            startDate = LocalDate.of(2000, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now().plusDays(1);
        }

        List<SwIndustryDaily> list =
                industryDailyRepository.findByIndustryCodeInAndTradeDateBetweenOrderByIndustryCodeAscTradeDateAsc(
                        industryCodes,
                        startDate,
                        endDate
                );

        // 按行业分组
        Map<String, List<SwIndustryDaily>> grouped =
                list.stream().collect(Collectors.groupingBy(SwIndustryDaily::getIndustryCode, LinkedHashMap::new, Collectors.toList()));

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        List<IndustrySeriesDto> result = new ArrayList<>();
        for (Map.Entry<String, List<SwIndustryDaily>> entry : grouped.entrySet()) {
            String code = entry.getKey();
            List<SwIndustryDaily> dailyList = entry.getValue();

            List<IndustryDailyPointDto> points = dailyList.stream().map(d -> {
                IndustryDailyPointDto dto = new IndustryDailyPointDto();
                long millis = d.getTradeDate()
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli();
                dto.setDate(millis);
                dto.setAddLyrPeQuantile(d.getAddLyrPeQuantile());
                dto.setAddTtmPeQuantile(d.getAddTtmPeQuantile());
                dto.setAddPbQuantile(d.getAddPbQuantile());
                dto.setPe(d.getPe());
                dto.setPeTtm(d.getPeTtm());
                dto.setPb(d.getPb());
                dto.setIndexClose(d.getIndexClose());
                return dto;
            }).collect(Collectors.toList());

            IndustrySeriesDto series = new IndustrySeriesDto();
            series.setIndustryCode(code);
            series.setIndustryName(null); // TODO: 以后你从行业表补充
            series.setRows(points);

            result.add(series);
        }

        return result;
    }

    /**
     * 把逗号分隔的 codes 字符串转成 List
     */
    public List<String> parseCodes(String codesStr) {
        if (!StringUtils.hasText(codesStr)) {
            return Collections.emptyList();
        }
        return Arrays.stream(codesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
}
