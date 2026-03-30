import React, { useState, useEffect } from "react";
import { 
  Card, Typography, Form, Input, Button, Upload, message, 
  Divider, Row, Col, Avatar, Badge, Tag, Modal, Spin
} from "antd";
import { 
  SaveOutlined, UserOutlined, MailOutlined, PhoneOutlined, 
  HomeOutlined, CameraOutlined, LockOutlined, EditOutlined,
  CheckCircleOutlined
} from "@ant-design/icons";
import { userAPI } from "../../services/api";
import axiosInstance from "../../services/api";

const { Title, Text } = Typography;

const ProfilePage = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(false);
    
    // Avatar states
    const [avatarFile, setAvatarFile] = useState(null);
    const [previewAvatar, setPreviewAvatar] = useState(null);
    const [uploadingAvatar, setUploadingAvatar] = useState(false);

    // Edit states
    const [isEditingInfo, setIsEditingInfo] = useState(false);
    const [isEditingPassword, setIsEditingPassword] = useState(false);
    
    // Password / OTP states
    const [passwordLoading, setPasswordLoading] = useState(false);
    const [otpModalVisible, setOtpModalVisible] = useState(false);
    const [otpLoading, setOtpLoading] = useState(false);
    const [pendingNewPassword, setPendingNewPassword] = useState("");
    const [otpValue, setOtpValue] = useState("");

    const [infoForm] = Form.useForm();
    const [passwordForm] = Form.useForm();

    useEffect(() => {
        const userData = localStorage.getItem("user");
        if (userData) {
            const parsedUser = JSON.parse(userData);
            setUser(parsedUser);
            infoForm.setFieldsValue(parsedUser);
            setPreviewAvatar(parsedUser.avatarUrl);
        }
    }, [infoForm]);

    // ---- THÔNG TIN CÁ NHÂN & AVATAR ----
    const onSaveInfo = async (values) => {
        setLoading(true);
        try {
            const response = await userAPI.updateProfile(user.id, values);
            let updatedUser = response.data;
            
            if (avatarFile) {
                const formData = new FormData();
                formData.append("file", avatarFile);
                const avatarResponse = await axiosInstance.post(`/users/${user.id}/avatar`, formData, {
                    headers: { "Content-Type": "multipart/form-data" },
                });
                updatedUser = avatarResponse.data;
            }

            localStorage.setItem("user", JSON.stringify(updatedUser));
            setUser(updatedUser);
            setIsEditingInfo(false);
            setAvatarFile(null);
            message.success("Cập nhật thông tin thành công!");
            setTimeout(() => window.location.reload(), 800);
        } catch (error) {
            message.error(error.response?.data?.message || "Cập nhật thất bại!");
        } finally {
            setLoading(false);
        }
    };

    const handleAvatarChange = (info) => {
        if (info.file) {
            setAvatarFile(info.file);
            setPreviewAvatar(URL.createObjectURL(info.file));
            
            // Nếu người dùng chọn ảnh mà chưa bấm Sửa thông tin, tự động mở chế độ sửa để có nút Lưu
            if (!isEditingInfo) {
                setIsEditingInfo(true);
            }
        }
    };

    // ---- ĐỔI MẬT KHẨU ----
    const onRequestPasswordChange = async (values) => {
        setPasswordLoading(true);
        try {
            // Yêu cầu lấy OTP
            await axiosInstance.post(`/users/${user.id}/request-password-otp`, {
                currentPassword: values.currentPassword
            });
            setPendingNewPassword(values.newPassword);
            setOtpModalVisible(true);
            message.success("Mã xác nhận (OTP) đã được gửi đến email của bạn.");
        } catch (error) {
            message.error(error.response?.data?.message || "Mật khẩu hiện tại không đúng!");
        } finally {
            setPasswordLoading(false);
        }
    };

    const confirmPasswordChange = async () => {
        if (!otpValue || otpValue.length !== 6) {
            message.warning("Vui lòng nhập đủ 6 số OTP");
            return;
        }
        setOtpLoading(true);
        try {
            await axiosInstance.post(`/users/${user.id}/confirm-password-change`, {
                otp: otpValue,
                newPassword: pendingNewPassword
            });
            message.success("Đổi mật khẩu thành công!");
            setOtpModalVisible(false);
            setIsEditingPassword(false);
            passwordForm.resetFields();
            setOtpValue("");
            setPendingNewPassword("");
        } catch (error) {
            message.error(error.response?.data?.message || "Mã OTP không đúng hoặc đã hết hạn!");
        } finally {
            setOtpLoading(false);
        }
    };

    if (!user) return null;

    return (
        <div className="bg-gray-50 min-h-screen py-12">
            <div className="container mx-auto px-4 max-w-5xl">
                <Title level={2} className="!mb-8 font-black uppercase tracking-tight text-gray-800">Cài đặt tài khoản</Title>

                <Row gutter={24}>
                    {/* Left: Avatar & Summary */}
                    <Col xs={24} md={8}>
                        <Card className="rounded-[32px] shadow-sm border-none text-center p-6 mb-6">
                            <div className="relative inline-block group">
                                <Avatar 
                                    size={160} 
                                    src={previewAvatar} 
                                    icon={<UserOutlined />} 
                                    className="border-4 border-orange-50 shadow-xl object-cover"
                                />
                                {/* Nút thay đổi avatar nằm chính giữa */}
                                <Upload 
                                    showUploadList={false} 
                                    beforeUpload={() => false}
                                    onChange={handleAvatarChange}
                                >
                                    <div className="absolute inset-0 flex items-center justify-center bg-black/40 rounded-full opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                                        <CameraOutlined className="text-white text-3xl" />
                                    </div>
                                </Upload>
                            </div>
                            
                            {avatarFile && isEditingInfo && (
                                <div className="mt-2 text-orange-500 font-medium text-sm">
                                    Ảnh đã chọn. Vui lòng nhấn "Lưu Thông Tin" để cập nhật.
                                </div>
                            )}
                            
                            <div className="mt-6">
                                <Title level={4} className="!mb-1 font-bold">{user.fullName}</Title>
                                <Tag color="orange" className="rounded-full px-4 border-none font-bold uppercase text-[10px]">
                                    {user.role}
                                </Tag>
                            </div>

                            <Divider className="my-6 border-gray-100" />
                            
                            <div className="text-left space-y-4">
                                <div className="flex items-center gap-3 text-gray-500">
                                    <MailOutlined className="text-orange-500" />
                                    <Text className="text-sm truncate">{user.email}</Text>
                                </div>
                                <div className="flex items-center gap-3 text-gray-500">
                                    <PhoneOutlined className="text-orange-500" />
                                    <Text className="text-sm">{user.phoneNumber || "Chưa cập nhật"}</Text>
                                </div>
                            </div>
                        </Card>
                    </Col>

                    {/* Right: Forms */}
                    <Col xs={24} md={16}>
                        {/* Box Thông tin cá nhân */}
                        <Card className="rounded-[32px] shadow-sm border-none overflow-hidden mb-6">
                            <div className="flex justify-between items-center mb-6">
                                <Title level={4} className="!m-0 font-black text-xs uppercase text-gray-400 tracking-widest">
                                    <UserOutlined className="mr-2"/> Thông tin cá nhân
                                </Title>
                                {!isEditingInfo ? (
                                    <Button type="primary" ghost icon={<EditOutlined />} onClick={() => setIsEditingInfo(true)} className="rounded-lg font-semibold border-primary text-primary">
                                        Sửa thông tin
                                    </Button>
                                ) : (
                                    <Button type="text" onClick={() => { setIsEditingInfo(false); infoForm.resetFields(); }} className="text-gray-500 font-semibold">Hủy</Button>
                                )}
                            </div>

                            <Form 
                                form={infoForm} 
                                layout="vertical" 
                                onFinish={onSaveInfo}
                                disabled={!isEditingInfo}
                            >
                                <Row gutter={16}>
                                    <Col span={24}>
                                        <Form.Item label="Họ và tên" name="fullName" rules={[{ required: true }]}>
                                            <Input prefix={<UserOutlined className="text-gray-400"/>} className={`h-12 rounded-2xl ${isEditingInfo ? 'bg-white' : 'bg-gray-50 border-transparent'} border-gray-200`} />
                                        </Form.Item>
                                    </Col>
                                    <Col span={24}>
                                        <Form.Item label="Email (Không thể thay đổi)" name="email">
                                            <Input disabled prefix={<MailOutlined className="text-gray-400"/>} className="h-12 rounded-2xl opacity-60 bg-gray-50 border-transparent" />
                                        </Form.Item>
                                    </Col>
                                    <Col span={24}>
                                        <Form.Item label="Số điện thoại" name="phoneNumber">
                                            <Input prefix={<PhoneOutlined className="text-gray-400"/>} className={`h-12 rounded-2xl ${isEditingInfo ? 'bg-white' : 'bg-gray-50 border-transparent'} border-gray-200`} />
                                        </Form.Item>
                                    </Col>
                                    <Col span={24}>
                                        <Form.Item label="Địa chỉ giao hàng mặc định" name="address">
                                            <Input.TextArea rows={3} className={`rounded-2xl p-4 ${isEditingInfo ? 'bg-white' : 'bg-gray-50 border-transparent'} border-gray-200`} />
                                        </Form.Item>
                                    </Col>
                                </Row>

                                {isEditingInfo && (
                                    <div className="mt-4 flex justify-end">
                                        <Button 
                                            type="primary" 
                                            htmlType="submit" 
                                            loading={loading}
                                            icon={<SaveOutlined />}
                                            className="h-12 px-8 rounded-xl bg-gradient-to-r from-orange-500 to-red-600 border-none font-bold shadow-lg shadow-orange-100"
                                        >
                                            LƯU THÔNG TIN
                                        </Button>
                                    </div>
                                )}
                            </Form>
                        </Card>

                        {/* Box Đổi mật khẩu */}
                        <Card className="rounded-[32px] shadow-sm border-none overflow-hidden">
                            <div className="flex justify-between items-center mb-6">
                                <Title level={4} className="!m-0 font-black text-xs uppercase text-gray-400 tracking-widest">
                                    <LockOutlined className="mr-2"/> Bảo mật tài khoản
                                </Title>
                                {!isEditingPassword ? (
                                    <Button type="default" icon={<EditOutlined />} onClick={() => setIsEditingPassword(true)} className="rounded-lg font-semibold">
                                        Đổi mật khẩu
                                    </Button>
                                ) : (
                                    <Button type="text" onClick={() => { setIsEditingPassword(false); passwordForm.resetFields(); }} className="text-gray-500 font-semibold">Hủy</Button>
                                )}
                            </div>

                            {isEditingPassword ? (
                                <Form 
                                    form={passwordForm} 
                                    layout="vertical" 
                                    onFinish={onRequestPasswordChange}
                                >
                                    <Row gutter={16}>
                                        <Col span={24}>
                                            <Form.Item label="Mật khẩu hiện tại" name="currentPassword" rules={[{ required: true, message: "Nhập mật khẩu hiện tại" }]}>
                                                <Input.Password prefix={<LockOutlined className="text-gray-400"/>} className="h-12 rounded-2xl bg-white border-gray-200" />
                                            </Form.Item>
                                        </Col>
                                        <Col span={24}>
                                            <Form.Item label="Mật khẩu mới (Tối thiểu 6 ký tự)" name="newPassword" rules={[{ required: true, min: 6, message: "Nhập mật khẩu mới hợp lệ" }]}>
                                                <Input.Password prefix={<LockOutlined className="text-gray-400"/>} className="h-12 rounded-2xl bg-white border-gray-200" />
                                            </Form.Item>
                                        </Col>
                                    </Row>

                                    <div className="mt-2 flex justify-end">
                                        <Button 
                                            type="primary" 
                                            htmlType="submit" 
                                            loading={passwordLoading}
                                            icon={<CheckCircleOutlined />}
                                            className="h-12 px-8 rounded-xl bg-gray-900 border-none font-bold shadow-lg hover:bg-gray-800"
                                        >
                                            XÁC NHẬN ĐỔI MẬT KHẨU
                                        </Button>
                                    </div>
                                </Form>
                            ) : (
                                <div className="bg-gray-50 p-4 rounded-2xl text-sm text-gray-500">
                                    <LockOutlined className="mr-2" /> Mật khẩu của bạn được bảo mật bởi hệ thống EXEShop. 
                                </div>
                            )}
                        </Card>
                    </Col>
                </Row>
            </div>

            {/* Modal Nhập mã OTP */}
            <Modal
                title="Xác minh bảo mật (OTP)"
                open={otpModalVisible}
                onCancel={() => setOtpModalVisible(false)}
                footer={null}
                centered
            >
                <div className="text-center py-6">
                    <MailOutlined className="text-5xl text-orange-500 mb-4" />
                    <Title level={4}>Kiểm tra Email của bạn</Title>
                    <Text className="block text-gray-500 mb-6">
                        Chúng tôi đã gửi một mã xác minh gồm 6 chữ số đến email <b>{user.email}</b>. 
                        Bạn hãy nhập mã này để tạo mật khẩu mới.
                    </Text>
                    
                    <Input 
                        size="large" 
                        placeholder="Nhập 6 số OTP" 
                        maxLength={6}
                        className="text-center text-2xl font-black tracking-widest h-14 w-48 mb-6 bg-gray-50 rounded-xl"
                        value={otpValue}
                        onChange={(e) => setOtpValue(e.target.value)}
                    />
                    
                    <Button 
                        type="primary" 
                        className="w-full h-12 bg-orange-500 border-none font-bold rounded-xl shadow-lg"
                        loading={otpLoading}
                        onClick={confirmPasswordChange}
                    >
                        HOÀN TẤT ĐỔI MẬT KHẨU
                    </Button>
                </div>
            </Modal>
        </div>
    );
};

export default ProfilePage;
