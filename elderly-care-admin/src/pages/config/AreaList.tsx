import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Modal, Form, Input, Select, Tree } from 'antd';
import type { TreeDataNode } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getConfigList, createConfig, updateConfig, deleteConfig, getRegions } from '../../api/config';
import type { Config } from '../../types';
import { formatDate } from '../../utils';

const AreaList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Config[]>([]);
  const [treeData, setTreeData] = useState<TreeDataNode[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingConfig, setEditingConfig] = useState<Config | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const [configRes, regionRes] = await Promise.all([
        getConfigList({ page, pageSize, category: 'area' }),
        getRegions(),
      ]);
      setDataSource(configRes.data.list);
      setTreeData(convertToTree(regionRes.data));
      setPagination({
        current: configRes.data.page,
        pageSize: configRes.data.pageSize,
        total: configRes.data.total,
      });
    } catch {
      message.error('获取区域列表失败');
    } finally {
      setLoading(false);
    }
  };

  const convertToTree = (items: Config[]): TreeDataNode[] => {
    const map: Record<string, TreeDataNode> = {};
    const roots: TreeDataNode[] = [];

    items.forEach((item) => {
      map[item.code] = { key: item.code, title: item.name, children: [] };
    });

    items.forEach((item) => {
      if (item.remark && map[item.remark]) {
        map[item.remark].children?.push(map[item.code]);
      } else {
        roots.push(map[item.code]);
      }
    });

    return roots;
  };

  const handleTableChange = (pag: { current: number; pageSize: number }) => {
    fetchData(pag.current, pag.pageSize);
  };

  const handleAdd = () => {
    setEditingConfig(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Config) => {
    setEditingConfig(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该区域吗？',
      onOk: async () => {
        try {
          await deleteConfig(id);
          message.success('删除成功');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingConfig) {
        await updateConfig(editingConfig.id, values);
        message.success('更新成功');
      } else {
        await createConfig({ ...values, category: 'area' });
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const columns: ColumnsType<Config> = [
    { title: '区域名称', dataIndex: 'name', key: 'name' },
    { title: '区域编码', dataIndex: 'code', key: 'code' },
    { title: '上级区域', dataIndex: 'remark', key: 'remark', render: (text) => text || '-' },
    { title: '排序', dataIndex: 'sort', key: 'sort' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
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
      title="区域管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增区域
          </Button>
        </Space>
      }
    >
      <div style={{ marginBottom: 16 }}>
        <h4>区域树</h4>
        <Tree treeData={treeData} defaultExpandAll />
      </div>

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
        title={editingConfig ? '编辑区域' : '新增区域'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="区域名称" rules={[{ required: true, message: '请输入区域名称' }]}>
            <Input placeholder="请输入区域名称" />
          </Form.Item>
          <Form.Item name="code" label="区域编码" rules={[{ required: true, message: '请输入区域编码' }]}>
            <Input placeholder="请输入区域编码" />
          </Form.Item>
          <Form.Item name="remark" label="上级区域">
            <Input placeholder="请输入上级区域编码" />
          </Form.Item>
          <Form.Item name="sort" label="排序" initialValue={0}>
            <Input type="number" placeholder="请输入排序号" />
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

export default AreaList;
