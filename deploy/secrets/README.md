# Secrets Configuration

This directory contains template files for secrets required by SAST AI.

## Setup Instructions

### 1. S3/MinIO Credentials

Copy the example file and fill in your credentials:

```bash
cp s3-credentials.env.example s3-credentials.env
```

Edit `s3-credentials.env` with your actual S3/MinIO credentials:
- `AWS_ACCESS_KEY_ID`: Your S3 access key
- `AWS_SECRET_ACCESS_KEY`: Your S3 secret key
- `AWS_S3_ENDPOINT_URL`: S3 endpoint URL (e.g., `https://minio.example.com` for MinIO)

**Note**: These environment variables will be mapped to Kubernetes secret keys:
- `AWS_ACCESS_KEY_ID` → `access_key_id`
- `AWS_SECRET_ACCESS_KEY` → `secret_access_key`
- `AWS_S3_ENDPOINT_URL` → `endpoint_url`

### 2. Google Service Account

Copy the example file and add your service account JSON:

```bash
cp google-service-account.json.example google-service-account.json
```

Edit `google-service-account.json` with your actual Google Cloud service account credentials.

You can get this file from:
1. Google Cloud Console → IAM & Admin → Service Accounts
2. Create or select a service account
3. Keys → Add Key → Create New Key → JSON

### 3. Deploy

Once both files are configured, run the deployment:

```bash
make deploy-dev   # Development
make deploy-prod  # Production
```

The Makefile will automatically create the secrets from these files.

## Security Notes

⚠️ **IMPORTANT**: 
- The actual secret files (`*.env` and `*.json` without `.example`) are gitignored
- Never commit actual credentials to version control
- Keep these files secure with appropriate file permissions (chmod 600)
- Rotate credentials regularly
- Use different credentials for dev and prod environments

## File Permissions

Set restrictive permissions on your secret files:

```bash
chmod 600 s3-credentials.env
chmod 600 google-service-account.json
```

