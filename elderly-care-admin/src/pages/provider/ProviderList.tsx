import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Modal, Form, Input, Select } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getProviderList, createProvider, updateProvider, deleteProvider, enableProvider, disableProvider } from '../../api/provider';
import type { Provider, ProviderQuery } from '../../types';
import { formatDate } from '../../utils';

const ProviderList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Provider[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProvider, setEditingProvider] = useState<Provider | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getProviderList({ page, pageSize } as ProviderQuery);
      setDataSource(response.data.list);
      setPagination({
        current: response.data.page,
        pageSize: response.data.pageSize,
        total: response.data.total,
      });
    } catch {
      message.error('获取服务商列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    if (pag.current && pag.pageSize) {
      fetchData(pag.current, pag.pageSize);
    }
  };

  const handleAdd = () => {
    setEditingProvider(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Provider) => {
    setEditingProvider(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该服务商吗？',
      onOk: async () => {
        try {
          await deleteProvider(id);
          message.success('删除成功');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const handleToggleStatus = async (id: number, currentStatus: number) => {
    try {
      if (currentStatus === 1) {
        await disableProvider(id);
      } else {
        await enableProvider(id);
      }
      message.success('状态更新成功');
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('状态更新失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingProvider) {
        await updateProvider(editingProvider.id, values);
        message.success('更新成功');
      } else {
        await createProvider(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const getStatusTag = (status: number) => {
    switch (status) {
      case 0: return <Tag color="red">禁用</Tag>;
      case 1: return <Tag color="green">启用</Tag>;
      case 2: return <Tag color="orange">待审核</Tag>;
      case 3: return <Tag color="red">审核拒绝</Tag>;
      default: return <Tag>未知</Tag>;
    }
  };

  const columns: ColumnsType<Provider> = [
    { title: '服务商名称', dataIndex: 'name', key: 'name' },
    { title: '联系人', dataIndex: 'legalPerson', key: 'legalPerson' },
    { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone' },
    {
      title: '服务类型',
      dataIndex: 'serviceScope',
      key: 'serviceScope',
      render: (scope: string[]) => scope?.map(s => <Tag key={s} color="blue">{s}</Tag>) || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => getStatusTag(status),
    },
    { title: '评分', dataIndex: 'rating', key: 'rating', render: (rating: number) => rating ? `${rating} 星` : '-' },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={record.status === 1 ? <CloseOutlined /> : <CheckOutlined />}
            onClick={() => handleToggleStatus(record.id, record.status)}
          >
            {record.status === 1 ? '禁用' : '启用'}
          </Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="服务商管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增服务商
          </Button>
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange}
      />

      <Modal
        title={editingProvider ? '编辑服务商' : '新增服务商'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="服务商名称" rules={[{ required: true, message: '请输入服务商名称' }]}>
            <Input placeholder="请输入服务商名称" />
          </Form.Item>
          <Form.Item name="code" label="服务商编码" rules={[{ required: true, message: '请输入服务商编码' }]}>
            <Input placeholder="请输入服务商编码" />
          </Form.Item>
          <Form.Item name="legalPerson" label="联系人">
            <Input placeholder="请输入联系人" />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input.TextArea placeholder="请输入地址" rows={2} />
          </Form.Item>
          <Form.Item name="status" label="状态" initialValue={1}>
            <Select>
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default ProviderList;
