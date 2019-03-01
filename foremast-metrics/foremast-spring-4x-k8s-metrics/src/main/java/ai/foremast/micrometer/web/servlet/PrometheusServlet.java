/**
 * Licensed to the Foremast under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.foremast.micrometer.web.servlet;


import ai.foremast.metrics.k8s.starter.CommonMetricsFilter;
import ai.foremast.metrics.k8s.starter.K8sMetricsProperties;
import ai.foremast.micrometer.autoconfigure.MetricsProperties;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;


import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Prometheus Controller
 * @author Sheldon Shao
 * @version 1.0
 */
public class PrometheusServlet extends HttpServlet {

    private CollectorRegistry collectorRegistry;


    private CommonMetricsFilter commonMetricsFilter;


    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        this.collectorRegistry = ctx.getBean(CollectorRegistry.class);
        this.commonMetricsFilter = ctx.getBean(CommonMetricsFilter.class);
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (commonMetricsFilter != null && action != null) {
            String metricName = req.getParameter("metric");
            if ("enable".equalsIgnoreCase(action)) {
                commonMetricsFilter.enableMetric(metricName);
            }
            else if ("disable".equalsIgnoreCase(action)) {
                commonMetricsFilter.disableMetric(metricName);
            }
            resp.getWriter().write("OK");
            return;
        }

        try {
            StringWriter writer = new StringWriter();
            TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
            resp.setContentType(TextFormat.CONTENT_TYPE_004);
            resp.getWriter().write(writer.toString());
        } catch (IOException e) {
            // This actually never happens since StringWriter::write() doesn't throw any IOException
            throw new RuntimeException("Writing metrics failed", e);
        }
    }

}
