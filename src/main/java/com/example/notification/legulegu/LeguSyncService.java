package com.example.notification.legulegu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class LeguSyncService {

    private static final Logger log = LoggerFactory.getLogger(LeguSyncService.class);

    private final RestTemplate restTemplate;
    private final SWIndustryRepository industryRepository;
    private final SwIndustryDailyRepository dailyRepository;

    @Value("${legu.base-url:https://legulegu.com/api/stockdata/sw-industry-2021}")
    private String baseUrl;

    @Value("${legu.token}")
    private String token;

    @Value("${legu.cookie}")
    private String cookie;

    // 2 秒一个 industry
    private static final long SLEEP_MILLIS = 2000L;

    public LeguSyncService(RestTemplate restTemplate,
                           SWIndustryRepository industryRepository,
                           SwIndustryDailyRepository dailyRepository) {
        this.restTemplate = restTemplate;
        this.industryRepository = industryRepository;
        this.dailyRepository = dailyRepository;
    }

    /**
     * 全量同步 258 个行业
     * @param cleanBeforeImport true = 每个行业先删再写（简单粗暴）
     */
    public void syncAllIndustries(boolean cleanBeforeImport) {
        List<SWIndustry> industries = industryRepository.findAll();
        log.info("开始同步申万三级行业日线，共 {} 个行业", industries.size());

        for (SWIndustry industry : industries) {
            String code = industry.getIndustryCode();
            try {
                syncSingleIndustry(code, cleanBeforeImport);
            } catch (Exception e) {
                log.error("同步行业 {} 失败: {}", code, e.getMessage(), e);
            }

            try {
                Thread.sleep(SLEEP_MILLIS);   // 每个 industry 之间 sleep 2 秒
            } catch (InterruptedException ignored) {
            }
        }

        log.info("全部行业同步完成");
    }

    public void debugSingle(String industryCode) {
        String url = buildUrl(industryCode);

        HttpHeaders headers = new HttpHeaders();

        // 按你浏览器里的 Accept
        headers.add("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp," +
                        "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");

        // 这几个可以照抄
        headers.add("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,de;q=0.6");
        headers.add("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");

        // 关键：Cookie（把你浏览器里那一整串粘这里）
        headers.add("Cookie",
                "LAAA=6e678381-af6b-4de2-9166-10d8a1fd683a; Hm_lvt_4064402dbf370b44e70272f9f2632a67=1764993350; HMACCOUNT=F30EFE8D51753EC7; _gid=GA1.2.936125503.1764993351; _ga_5YD2TJWE1T=GS2.1.s1765016561$o7$g0$t1765016561$j60$l0$h0; _ga=GA1.1.301929353.1764993351; __gads=ID=e01aa476c21e898c:T=1764993375:RT=1765016560:S=ALNI_MZWZ_t613t2hU29ggMEwOrlMV62rQ; __gpi=UID=000011c3bda30c31:T=1764993375:RT=1765016560:S=ALNI_Mabqt6CbMVAt5H5tO1ogXoIgpAjdQ; __eoi=ID=4cd636fca7178e1b:T=1764993375:RT=1765016560:S=AA-AfjYH1WJowe4lRko6rYKkd5DZ; Hm_lpvt_4064402dbf370b44e70272f9f2632a67=1765016562; FCCDCF=%5Bnull%2Cnull%2Cnull%2Cnull%2Cnull%2Cnull%2C%5B%5B32%2C%22%5B%5C%224693604d-50ec-47db-a68c-86fcebb0fcc9%5C%22%2C%5B1764993376%2C177000000%5D%5D%22%5D%5D%5D; FCNEC=%5B%5B%22AKsRol_yyy9_SV6ttmUVnE8t17STZMq_bQOec9n7SA5Ds91cBQUk22hGFfozAx04IJwSbYt7cWGuXBUtWodPLYJu1wKQc5rnf9JqPbLCtNjkbfAeGHqld5hNwPPnlbxrcxud1pqcAkWv2eemTUEVOs7uYZjVo3hv4A%3D%3D%22%5D%5D; acw_tc=0b6e703217650309371085469eb9a5aa8e8ee589f6f3048a81d6d679a8179b; remember-me=L1lKZkI4UnpWTmlDSEZRaUtneUZHZz09OnRMU0JQSS9WZm81eFlLSFZSWjVNaGc9PQ; JSESSIONID=80F24F38B30379C46A6BF27E3B065400");

        // 下面这些不是绝对必须，但尽量和浏览器一致
        headers.add("Sec-Fetch-Dest", "document");
        headers.add("Sec-Fetch-Mode", "navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("Sec-Fetch-User", "?1");
        headers.add("Upgrade-Insecure-Requests", "1");
        headers.add("sec-ch-ua", "\"Chromium\";v=\"142\", \"Google Chrome\";v=\"142\", \"Not_A Brand\";v=\"99\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");

        // Accept-Encoding / Connection / Host 一般由底层 HTTP 客户端自动处理，可以不手动加
        // 如果你想完全一致，也可以：
        // headers.add("Accept-Encoding", "gzip, deflate, br, zstd");
        // headers.add("Connection", "keep-alive");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        log.info("url    = {}", url);
        log.info("status = {}", resp.getStatusCode());
        log.info("len    = {}", resp.getHeaders().getFirst("Content-Length"));
        log.info("body   = {}", resp.getBody());
    }



    /**
     * 同步单个行业
     */
    public void syncSingleIndustry(String industryCode, boolean cleanBeforeImport) {
        log.info("同步行业 {} ...", industryCode);

        if (cleanBeforeImport) {
            dailyRepository.deleteByIndustryCode(industryCode);
        }

        String url = buildUrl(industryCode);
        HttpHeaders headers = new HttpHeaders();

        // 按你浏览器里的 Accept
        headers.add("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp," +
                        "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");

        // 这几个可以照抄
        headers.add("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,de;q=0.6");
        headers.add("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");
        headers.add("Sec-Fetch-Dest", "document");
        headers.add("Sec-Fetch-Mode", "navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("Sec-Fetch-User", "?1");
        headers.add("Upgrade-Insecure-Requests", "1");
        headers.add("sec-ch-ua", "\"Chromium\";v=\"142\", \"Google Chrome\";v=\"142\", \"Not_A Brand\";v=\"99\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        // 关键：Cookie（把你浏览器里那一整串粘这里）
        headers.add("Cookie",
                cookie);


        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<LeguResponse> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, LeguResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            log.warn("行业 {} 请求失败，状态码: {}", industryCode, resp.getStatusCode());
            return;
        }

        List<LeguDataItem> dataList = resp.getBody().getData();
        if (dataList == null || dataList.isEmpty()) {
            log.warn("行业 {} 返回 data 为空", industryCode);
            return;
        }

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        int insertCount = 0;

        for (LeguDataItem item : dataList) {
            if (item.getDate() == null) continue;

            LocalDate tradeDate = Instant.ofEpochMilli(item.getDate())
                    .atZone(zoneId)
                    .toLocalDate();

            if (!cleanBeforeImport &&
                    dailyRepository.existsByIndustryCodeAndTradeDate(industryCode, tradeDate)) {
                continue;
            }

            SwIndustryDaily d = new SwIndustryDaily();
            d.setIndustryCode(industryCode);
            d.setTradeDate(tradeDate);

            // 原来的四个
            d.setPe(item.getLyrPe());
            d.setPeTtm(item.getTtmPe());
            d.setPb(item.getPb());
            d.setIndexClose(item.getIndexClose());

            // 新增的全部字段
            d.setLyrPeQuantile(item.getLyrPeQuantile());
            d.setTtmPeQuantile(item.getTtmPeQuantile());
            d.setPbQuantile(item.getPbQuantile());

            d.setDvRatio(item.getDvRatio());
            d.setDvRatioQuantile(item.getDvRatioQuantile());
            d.setDvTtm(item.getDvTtm());
            d.setDvTtmQuantile(item.getDvTtmQuantile());

            d.setAddLyrPe(item.getAddLyrPe());
            d.setAddLyrPeQuantile(item.getAddLyrPeQuantile());
            d.setAddTtmPe(item.getAddTtmPe());
            d.setAddTtmPeQuantile(item.getAddTtmPeQuantile());   // ★ TTM 百分位
            d.setAddPb(item.getAddPb());
            d.setAddPbQuantile(item.getAddPbQuantile());
            d.setAddDvRatio(item.getAddDvRatio());
            d.setAddDvTtm(item.getAddDvTtm());

            d.setTurnoverRate(item.getTurnoverRate());
            d.setTurnoverRateF(item.getTurnoverRateF());
            d.setAddTurnoverRate(item.getAddTurnoverRate());
            d.setAddTurnoverRateF(item.getAddTurnoverRateF());
            d.setTurnoverRateFQuantile(item.getTurnoverRateFQuantile());

            d.setTotalMv(item.getTotalMv());
            d.setClosePrice(item.getClose());
            d.setAddClose(item.getAddClose());

            d.setMiddleLyrPe(item.getMiddleLyrPe());
            d.setMiddleLyrPeQuantile(item.getMiddleLyrPeQuantile());
            d.setMiddleTtmPe(item.getMiddleTtmPe());
            d.setMiddleTtmPeQuantile(item.getMiddleTtmPeQuantile());
            d.setMiddlePb(item.getMiddlePb());
            d.setMiddlePbQuantile(item.getMiddlePbQuantile());

            d.setBelowNetAssetPercent(item.getBelowNetAssetPercent());
            d.setBelowNetAssetCount(item.getBelowNetAssetCount());
            d.setTotal(item.getTotal());

            dailyRepository.save(d);
            insertCount++;
        }


        log.info("行业 {} 同步完成，新增 {} 条记录", industryCode, insertCount);
    }
    private String buildUrl(String industryCode) {
        String codeEncoded = URLEncoder.encode(industryCode, StandardCharsets.UTF_8);
        String tokenEncoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return baseUrl + "?industryCode=" + codeEncoded + "&token=" + tokenEncoded;
    }
}
