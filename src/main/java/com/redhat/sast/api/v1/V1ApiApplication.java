package com.redhat.sast.api.v1;

import java.util.HashSet;
import java.util.Set;

import com.redhat.sast.api.v1.resource.HealthResource;
import com.redhat.sast.api.v1.resource.JobBatchResource;
import com.redhat.sast.api.v1.resource.JobResource;
import com.redhat.sast.api.v1.resource.MlOpsJobBatchResource;
import com.redhat.sast.api.v1.resource.PackageResource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class V1ApiApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();

        // Add all v1 resources here
        resources.add(JobBatchResource.class);
        resources.add(JobResource.class);
        resources.add(MlOpsJobBatchResource.class);
        resources.add(PackageResource.class);
        resources.add(HealthResource.class);

        // Add any v1-specific providers (exception mappers, etc.)
        // resources.add(V1ExceptionMapper.class);

        return resources;
    }
}
