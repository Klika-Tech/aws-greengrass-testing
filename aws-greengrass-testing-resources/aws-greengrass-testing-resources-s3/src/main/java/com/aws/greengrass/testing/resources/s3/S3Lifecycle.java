package com.aws.greengrass.testing.resources.s3;

import com.aws.greengrass.testing.resources.AWSResourceLifecycle;
import com.aws.greengrass.testing.resources.AbstractAWSResourceLifecycle;
import com.google.auto.service.AutoService;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import javax.inject.Inject;

@AutoService(AWSResourceLifecycle.class)
public class S3Lifecycle extends AbstractAWSResourceLifecycle<S3Client> {
    @Inject
    public S3Lifecycle(S3Client client) {
        super(client, S3ObjectSpec.class, S3BucketSpec.class);
    }

    public S3Lifecycle() {
        this(S3Client.create());
    }

    public boolean bucketExists(String bucketName) {
        try {
            client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
