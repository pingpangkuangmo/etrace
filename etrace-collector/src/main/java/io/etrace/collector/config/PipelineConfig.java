/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.collector.config;

import io.etrace.common.pipeline.PipelineRepository;
import io.etrace.common.pipeline.impl.DefaultPipelineLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

//@Configuration
//todo : delete
public class PipelineConfig implements BeanFactoryAware {
    @Autowired
    private CollectorProperties collectorProperties;
    private BeanFactory beanFactory;

    @Bean
    @Primary
    public PipelineRepository pipelineRepository() throws Exception {
        return beanFactory.getBean(PipelineRepository.class, new DefaultPipelineLoader().load(),
            collectorProperties.getResources());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
