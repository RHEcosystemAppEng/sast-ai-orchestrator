package com.redhat.sast.ai.api.v1;

import com.redhat.sast.ai.api.v1.resource.HealthResource;
import com.redhat.sast.ai.api.v1.resource.JobBatchResource;
import com.redhat.sast.ai.api.v1.resource.JobResource;
import com.redhat.sast.ai.api.v1.resource.PackageResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class V1ApiApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // Add all v1 resources here
        resources.add(JobBatchResource.class);
        resources.add(JobResource.class);
        resources.add(PackageResource.class);
        resources.add(HealthResource.class);
        
        // Add any v1-specific providers (exception mappers, etc.)
        // resources.add(V1ExceptionMapper.class);
        
        return resources;
    }
} 