type = "csi"
id   = "hive-metastore-mysql"
name = "hive-metastore-mysql"
external_id     = "0001-0024-62c42aed-9839-4da6-8c09-9d220f56e924-0000000000000009-00000000-1111-2222-bbbb-cacacacacac1"
access_mode     = "single-node-writer"
attachment_mode = "file-system"
mount_options {
  fs_type = "ext4"
}
plugin_id       = "ceph-csi"
secrets {
  userID  = "admin"
  userKey = "AQAvo5JgP++oEhAAeZb1j/MTWyLGGJC6abCNFw=="
}
context {
  clusterID = "62c42aed-9839-4da6-8c09-9d220f56e924"
  pool      = "myPool"
  imageFeatures = "layering"
}