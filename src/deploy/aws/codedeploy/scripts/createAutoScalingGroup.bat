rem launchConfigurationName = VertxLaunchConfiguration
rem instantID = ami-6869aa05
rem keyName = VA-North
rem iamInstanceProfile = CodeDeployDemo-EC2
rem instanceType = t1.micro
aws autoscaling create-launch-configuration --launch-configuration-name VertxLaunchConfiguration --image-id ami-ea26ce85 --key-name keyName --iam-instance-profile CodeDeployDemo-EC2 --instance-type t1.micro --user-data file:user-data.txt